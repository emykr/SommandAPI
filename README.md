[![](https://jitpack.io/v/emykr/SommandAPI.svg)](https://jitpack.io/#emykr/SommandAPI)

# SommandAPI

SommandAPI 는 Bukkit/Paper 환경에서 선언형 DSL 로 명령 트리를 정의하고 자동으로 권한/등록/실행을 처리하는 경량 라이브러리입니다.


## 설치 (Installation)

Gradle (Kotlin DSL):

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.emykr:SommandAPI:v1.7.3")
}
```

Gradle (Groovy):

```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.emykr:SommandAPI:v1.7.3"
}
```

Maven:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.emykr</groupId>
    <artifactId>SommandAPI</artifactId>
    <version>v1.7.2</version>
  </dependency>
</dependencies>
```

---

## 빠른 시작

`JavaPlugin` 안에서 Loader 사용:

```kotlin
class ExamplePlugin : JavaPlugin() {
    override fun onEnable() {
        SommandLoader(this).load {
            // 가장 단순한 커맨드: /ping
            simple("ping", description = "Check latency") {
                sender.sendMessage("Pong!")
            }

            // 별칭 포함 root: /greet, /hello
            command("greet", "hello", description = "Send a greeting") {
                // /greet <name>
                argumentExec(StringArgument("target", greedy = false)) {
                    val target = arg<String>("target")
                    sender.sendMessage("Hello, $target!")
                }

                // /greet shout <name>
                literal("shout") {
                    argumentExec(StringArgument("target")) {
                        val target = arg<String>("target")
                        sender.sendMessage("HELLO, ${target.uppercase()}!!")
                    }
                }
            }
        }
    }
}
```

위 예제에서 사용한 `StringArgument` / `arg<T>()` 는 실제 구현에 맞는 API 명칭으로 치환하세요. (예: `CommandArgument.string("target")`, `get<T>("target")` 등)

---

## DSL 구조 상세

패턴(의미상):

```kotlin
command(vararg aliases, description = "...", permission = "node") {
    // 실행 지점 지정
    executes {
        // /<root> 직접 실행
    }

    // 하위 리터럴
    literal("sub") {
        // /<root> sub ...
    }

    // 리터럴 + 바로 실행
    literalExec("info", description = "Show info") {
        // /<root> info
    }

    // 인자 노드 (후속 분기 가능)
    argument(SomeArgument("value")) {
        literalExec("detail") {
            // /<root> <value> detail
        }
    }

    // 인자 + 즉시 실행
    argumentExec(SomeArgument("amount")) {
        // /<root> <amount>
    }

    // 그룹 (실제로는 literal sugar)
    group("admin") {
        literalExec("reload", permission = "plugin.reload") {
            // /<root> admin reload
        }
    }
}
```

요약:
- `literal()` : 토큰과 정확히 일치하는 분기
- `argument()` : 파싱 가능한 토큰 수용
- `...Exec` 변형: 자식 없는 즉시 실행
- `executes {}` : 현재 노드 자체 실행
- `permission` 파라미터: 해당 노드 진입/실행 시 퍼미션 필요
- `simple()` : 최소 정의 (root 실행만 있는 짧은 명령)

---

## 권한(Permission) 처리

`SommandLoader` 는 모든 등록된 트리를 순회하여 `node.permission` 이 있는 노드마다 Bukkit `PluginManager` 에 Permission 을 (없으면) 등록합니다. 기본 `PermissionDefault.OP` 로 생성되며, 서버 관리자가 `plugin.yml` 없이도 제어할 수 있게 합니다.

권장:
- Root 또는 실행이 가능한 핵심 노드에만 퍼미션 지정 (과도한 노드 퍼미션은 유지 부담 증가)
- 네이밍 규칙: `plugin.command.action` (예: `myplugin.user.delete`)

---

## 인자 (CommandArgument) 정의

라이브러리에서 제공하는 기본 Argument 가 있다면 해당 목록을 나열하세요. 기본 구현이 없거나 커스텀만 사용하는 구조라면 다음과 같이 직접 확장:

```kotlin
class IntArgument(
    val name: String,
    private val min: Int? = null,
    private val max: Int? = null
) : CommandArgument<Int> {

    override val id: String = name

    override fun parse(input: String, context: CommandArgument.Context): CommandArgument.Result<Int> {
        val value = input.toIntOrNull()
            ?: return CommandArgument.Result.failure("Expected integer for '$name'.")
        if (min != null && value < min) return CommandArgument.Result.failure("Value <$min")
        if (max != null && value > max) return CommandArgument.Result.failure("Value >$max")
        return CommandArgument.Result.success(value)
    }

    override fun suggestions(prefix: String, context: CommandArgument.Context): List<String> {
        // 숫자 인자에 대한 일반 자동완성은 제한적이므로 prefix 가 숫자로 시작할 때만 반환
        return if (prefix.all { it.isDigit() }) listOf(prefix) else emptyList()
    }
}
```

문서화 체크리스트:
- `parse()` 실패 시 메시지 표준화 (예: "Invalid <type>: <value>")
- greedy 인자 (`greedy = true`) 지원 여부 및 동작 설명: 나머지 토큰 전체를 하나의 인자로 결합

---

## 실행 컨텍스트 (ExecutionScope)

`argumentExec { ... }`, `literalExec { ... }`, `executes { ... }` 블록 내부는 `SommandNode.ExecutionScope` (이름은 코드에 따라 다를 수 있음) 입니다. 여기에 일반적으로 포함될 만한 것들:

| 메서드/프로퍼티 (예상) | 설명 |
|-----------------------|------|
| `sender` | `CommandSender` (또는 래핑 타입) |
| `arg<T>(name)` / `get<T>(name)` | 파싱된 인자 값 조회 |
| `fail(message)` | 실행 중단 + 피드백 |
| `reply(message)` | 간편 전송 |

실제 구현 메서드명을 확인 후 README 반영하세요.

---

## 퍼미션 / 접근 제어 흐름

1. Bukkit 커맨드 실행 직전 root.permission 검사 (Loader 내부)
2. Dispatcher 내부에서 트리 탐색 시 각 노드별 `permission` 검사
3. 불충분할 때 메시지: 기본 제공 또는 커스터마이즈 (Dispatcher / Hook 문서화 필요)

만약 사용자 맞춤 메시지를 원할 경우:
- Dispatcher 구현 (`SommandDispatcher` 인터페이스) 을 커스터마이즈하여 허용/거부, 오류 포맷 제어
- README 에 기본/커스텀 Dispatcher 예제 추가 (추후)

---

## 예: 하이브리드 명령

```kotlin
SommandLoader(plugin).load {
    command("user", description = "User management root") {

        // /user create <name>
        literal("create") {
            argumentExec(StringArgument("name"), permission = "myplugin.user.create") {
                val name = arg<String>("name")
                sender.sendMessage("Created user: $name")
            }
        }

        // /user delete <name>
        literal("delete", permission = "myplugin.user.delete") {
            argumentExec(StringArgument("name")) {
                val name = arg<String>("name")
                sender.sendMessage("Deleted user: $name")
            }
        }

        // /user list
        literalExec("list", permission = "myplugin.user.list") {
            // 예: 실제 목록 조회 로직
            sender.sendMessage("Users: Alice, Bob, Carol")
        }

        // /user stats <name> detail
        literal("stats") {
            argument(StringArgument("name")) {
                literalExec("detail") {
                    val name = arg<String>("name")
                    sender.sendMessage("Detail stats of $name ...")
                }
                executes {
                    val name = arg<String>("name")
                    sender.sendMessage("Basic stats of $name ...")
                }
            }
        }
    }
}
```

---

## 권장 스타일 가이드

- Root alias 첫 번째 이름: 가장 짧고 주 사용 명칭 (예: "user")
- Description: 모든 root 및 주요 literal 에 작성 (자동 도움말 기능 고려)
- Permission 은 최소 필요한 곳에만: root 광범위 제한 + 세부 행동 개별 허용 패턴 혹은 행동별 명시 패턴 둘 중 하나 선택, 혼용 최소화
- 인자 이름: 짧고 의미 명확 (userName → name)

---

## 에러/실패 케이스 문서화 권장 항목

| 상황 | 처리 전략 (예시) |
|------|------------------|
| 알 수 없는 서브 토큰 | "Unknown subcommand. Use /<root> help" |
| 인자 파싱 실패 | `parse()` 실패 메시지 노출 |
| 권한 부족 | "You do not have permission." (커스터마이즈 가능) |
| 실행 중 예외 | Dispatcher 에서 캐치 후 표준화 메시지 |

README 에 Dispatcher 커스터마이징 섹션이 필요하다면 실제 `SommandDispatcher` 코드 확인 후 추가하세요.

---

## Permission 자동 등록 동작

`SommandLoader.load { ... }` 호출 후:
1. `CommandRegistry.allDistinct()` 로 루트 노드 수집
2. 트리 DFS 로 각 노드의 `permission` 값 수집
3. Bukkit `PluginManager.getPermission(...) == null` 일 때만 새 Permission 생성 (`PermissionDefault.OP`)
4. 중복 문자열은 Set 으로 필터링

따라서 plugin.yml 에 미리 정의하지 않아도 OP 기본 값으로 등록되며, 서버 운영자는 `/permission`(혹은 권한 플러그인) 으로 제어 가능.

---

## API 확장 (예: 커스텀 Dispatcher)

(실제 `SommandDispatcher` 인터페이스 메서드 시그니처를 확인 후 아래 형식 보강)

```kotlin
class LoggingDispatcher : SommandDispatcher {
    override fun dispatch(root: RootNode, sender: CommandSender, label: String, args: List<String>) {
        // 1. 트리 탐색 (기본 구현 참고) 또는 기본 구현 감싸기
        // 2. 예외/로그 처리
    }
}
```

사용:

```kotlin
SommandLoader(this, dispatcher = LoggingDispatcher()).load {
    // commands ...
}
```

---

## FAQ

Q. plugin.yml 에 명령을 등록해야 하나요?  
A. 동적 등록이 작동하므로 plugin.yml에 등록은 선택입니다.

Q. 별칭(alias) 충돌 시 어떻게 되나요?  
A. `CommandRegistry.add()` 는 첫 등록 alias 를 기준으로 추가하며, 이미 존재하는 별칭은 덮어쓰지 않습니다(`putIfAbsent`). 충돌 가능성은 사전에 방지하세요.

Q. greedy 인자 뒤에 다른 노드를 둘 수 있나요?  
A. 일반적으로 greedy(남은 토큰 흡수) 뒤에는 추가 파싱이 불가능하므로 마지막에 위치시키는 것을 권장합니다. (실제 구현 확인 후 확정 표현)

---




