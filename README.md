[![](https://jitpack.io/v/emykr/SommandAPI.svg)](https://jitpack.io/#emykr/SommandAPI)

# SommandAPI

SommandAPI 는 Bukkit/Paper 환경에서 선언형 DSL 로 명령 트리를 정의하고 자동으로 권한/등록/실행을 처리하는 경량 라이브러리입니다.  
최근 버전에서 다음 사항이 추가/개선되었습니다:

- 명령 DSL 구조 분리: `SommandBuilder` (정의) 와 `sommand(plugin) { ... }` (엔트리 포인트)
- 버전 호환성 계층: `compat` 패키지(`BaseCompat`, `CompatResolver`, 버전별 `VersionCompat`)
- 인자 팩토리 강화: `Args` (유효성 검증 추가)
- 숫자 포매팅/출력 유틸: `NumberStrings` + 확장 함수 (`toLocalizedString`, `formatValue` 등)
- 가독성 & 실패 메시지 표준화

> 아래 예시/문서는 최신 DSL 형태(`sommand(plugin) { ... }`) 기준으로 정리되었습니다.  
> 기존 `SommandLoader(plugin).load { ... }` 코드는 하위 호환을 위해 유지될 수 있으나, 새 DSL 진입점 사용을 권장합니다.

---

## 설치 (Installation)

최신 릴리스 버전을 JitPack 에서 확인하세요: [SommandAPI Badge](https://jitpack.io/#emykr/SommandAPI)  
(아래 예시는 가상의 `v1.8.0` 로 표기; 실제 사용 시 최신 버전으로 변경)

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.emykr:SommandAPI:v1.8.0")
}
```

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.emykr:SommandAPI:v1.8.0"
}
```

### Maven

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
    <version>v1.8.0</version>
  </dependency>
</dependencies>
```

---

## 빠른 시작 (Quick Start)

`JavaPlugin` 안에서 DSL 사용:

```kotlin
import com.github.emykr.dsl.sommand
import com.github.emykr.dsl.Args

class ExamplePlugin : JavaPlugin() {
    override fun onEnable() {
        sommand(this) {
            // /ping
            simple("ping", description = "Check latency") {
                sender.sendMessage("Pong!")
            }

            // /greet /hello
            command("greet", "hello", description = "Send a greeting") {
                // /greet <name>
                argumentExec(Args.string("target")) {
                    val target = arg<String>("target")
                    sender.sendMessage("Hello, $target!")
                }

                // /greet shout <name>
                literal("shout") {
                    argumentExec(Args.string("target")) {
                        val target = arg<String>("target")
                        sender.sendMessage("HELLO, ${target.uppercase()}!!")
                    }
                }
            }
        }
    }
}
```

### 주요 차이점 (최근 변경)
| 항목 | 이전 | 현재 |
|------|------|------|
| 엔트리 포인트 | `SommandLoader(plugin).load { ... }` | `sommand(plugin) { ... }` |
| 버전 호환 | 없음 | `CompatResolver` 자동 선택 |
| 인자 팩토리 | 직접 `CommandArgument.*` 호출 | `Args.*` 래퍼 + 검증 |
| 숫자 출력 | 직접 포맷 구현 | `NumberStrings` / 확장 함수 |
| 구조 분리 | Loader + Builder 혼합 | 명확히 분리 (Builder & Loader 함수) |

---

## DSL 구조 상세

```kotlin
command(vararg aliases, description = "...", permission = "node") {
    executes { /* /<root> */ }

    literal("sub") { /* /<root> sub */ }

    literalExec("info", description = "Show info") {
        // /<root> info
    }

    argument(SomeArgument("value")) {
        literalExec("detail") {
            // /<root> <value> detail
        }
    }

    argumentExec(SomeArgument("amount")) {
        // /<root> <amount>
    }

    group("admin") {
        literalExec("reload", permission = "plugin.reload") {
            // /<root> admin reload
        }
    }
}
```

요약:
- `literal()` : 토큰과 정확히 일치
- `argument()` : 파싱 가능한 값 허용
- `literalExec` / `argumentExec` : 자식 없이 즉시 실행
- `executes {}` : 현재 노드 직접 실행
- `group()` : 실질적으로 literal sugar
- `simple()` : 최소 root + 실행

---

## 권한(Permission) 처리

1. Root 등록 시 root.permission 검사 (있다면 Bukkit Permission 자동 등록)
2. 실행/탐색 중 각 노드별 `permission` 검사
3. 실패 시 기본 메시지 (`BaseCompat.noPermissionMessage()`)

권장:
- 핵심 실행 지점만 퍼미션 부여
- 네이밍: `plugin.domain.action` (예: `myplugin.user.delete`)

---

## 인자 (Args / CommandArgument)

`Args` 유틸:

```kotlin
argumentExec(Args.int("amount", min = 1, max = 64)) { ... }
argumentExec(Args.enum<MyMode>("mode")) { ... }
argumentExec(Args.player("target")) { ... }
argumentExec(Args.greedyString("message")) { ... }
```

내부 검증:
- 이름 공백 불가
- 범위(min/max) 역전 시 예외
- enum / player 등 제네릭 정확성 유지

커스텀 예시:

```kotlin
class IntArgument(
    private val name: String,
    private val min: Int? = null,
    private val max: Int? = null
) : CommandArgument<Int> {
    override val id: String = name

    override fun parse(input: String, context: CommandArgument.Context): CommandArgument.Result<Int> {
        val v = input.toIntOrNull() ?: return CommandArgument.Result.failure("Expected integer for '$name'.")
        if (min != null && v < min) return CommandArgument.Result.failure("Value <$min")
        if (max != null && v > max) return CommandArgument.Result.failure("Value >$max")
        return CommandArgument.Result.success(v)
    }

    override fun suggestions(prefix: String, context: CommandArgument.Context): List<String> =
        if (prefix.all { it.isDigit() }) listOf(prefix) else emptyList()
}
```

---

## 실행 컨텍스트 (ExecutionScope)

명령 실행 블록(`executes`, `literalExec`, `argumentExec`) 내부에서 사용 가능한 대표 기능(구현에 따라 다를 수 있음):

| 기능(예상) | 설명 |
|-----------|------|
| `sender` | `CommandSender` |
| `arg<T>("name")` | 파싱된 인자 조회 |
| `reply(message)` | 메시지 전송 |
| `fail(message)` | 실행 중단 + 오류 표시 |

실제 클래스 / 메서드명을 코드 기준으로 README 재확인 후 반영하세요.

---

## 버전 호환 (Compat)

`CompatResolver.resolve()` 가 서버 버전(`1.21.4`, `1.21.9`, `1.21.10`, 기타) 감지 후 대응하는 `VersionCompat` 제공.  
확장/사용 예:

```kotlin
val compat = CompatResolver.resolve()
val dispatcher = compat.createDispatcher()
// dispatcher 를 커스텀 등록 로직에 사용 가능
```

커스터마이즈:
- 메시지 변경: `override fun noPermissionMessage() = "권한이 없습니다."`
- Dispatcher 교체: `override fun createDispatcher() = CustomDispatcher()`

---

## 숫자 포매팅 유틸 (NumberStrings)

숫자 출력 표준화:

```kotlin
val coins: Int? = 12345
val pretty = NumberStrings.withLocale(coins, Locale.US)          // "12,345"
val pattern = NumberStrings.withPattern(coins, "#,##0")          // "12,345"
val custom = NumberStrings.custom(coins) { n -> "${n} pts" }     // "12345 pts"
val tmpl  = NumberStrings.template("You have {c} coins.", "{c}", coins) // "You have 12345 coins."
```

Bukkit 객체 메시지:

```kotlin
sender.sendMessage(NumberStrings.forPlayerValue(player, 10, label = "lives"))
```

확장 함수(삭제/정리된 상태라면 필요한 경우 재도입):

```kotlin
coins?.let { NumberStrings.withLocale(it) }
```

---

## 예: 하이브리드 명령

```kotlin
sommand(plugin) {
    command("user", description = "User management root") {

        literal("create") {
            argumentExec(Args.string("name"), permission = "myplugin.user.create") {
                val name = arg<String>("name")
                sender.sendMessage("Created user: $name")
            }
        }

        literal("delete", permission = "myplugin.user.delete") {
            argumentExec(Args.string("name")) {
                val name = arg<String>("name")
                sender.sendMessage("Deleted user: $name")
            }
        }

        literalExec("list", permission = "myplugin.user.list") {
            sender.sendMessage("Users: Alice, Bob, Carol")
        }

        literal("stats") {
            argument(Args.string("name")) {
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

- 첫 번째 root alias: 짧고 주 사용 명칭
- Description: root 및 주요 literal 에 작성 (자동 도움말 대비)
- Permission: 최소 필요 지점에만
- 인자 이름: 짧고 의미 명확 (`userName` → `name`)

---

## 에러/실패 케이스 표준 메시지

| 상황 | 기본 처리 (예시) |
|------|------------------|
| 알 수 없는 토큰 | `BaseCompat.unknownSubcommandMessage(root)` |
| 인자 파싱 실패 | `Failed to parse argument: <detail>` |
| 권한 부족 | `You do not have permission.` |
| 실행 중 예외 | `An internal error occurred while attempting to perform this command.` |

커스터마이즈: 버전별 `VersionCompat` 에서 override.

---

## Permission 자동 등록 흐름

1. DSL 로드 후 모든 노드 순회
2. `permission != null` 인 노드 목록 수집
3. Bukkit `PluginManager.getPermission(str) == null` 인 경우 새 Permission 생성
4. 중복 필터링(Set)

---

## 커스텀 Dispatcher 예시

```kotlin
class LoggingDispatcher : SommandDispatcher {
    override fun dispatch(source: SommandSource, label: String, tokens: List<String>, root: SommandNode): Boolean {
        // 기존 DefaultDispatcher 로직 참고 후 래핑
        // 로깅 / 권한 메시지 커스터마이즈
        return true
    }

    override fun suggest(source: SommandSource, label: String, tokens: List<String>, root: SommandNode): List<String> {
        return emptyList()
    }
}
```

사용:

```kotlin
val compat = CompatResolver.resolve()
// compat.createDispatcher() 대신 new LoggingDispatcher() 전달 가능
```

---

## FAQ

**Q. plugin.yml 명령 등록이 필요한가요?**  
A. 동적 등록이 수행되므로 필수는 아닙니다.

**Q. greedy 인자 뒤에 다른 노드를 둘 수 있나요?**  
A. 일반적으로 greedy 인자가 나머지 토큰을 모두 소비하므로 마지막 위치 권장.

**Q. 퍼미션을 한 번에 재정의하고 싶습니다.**  
A. 커스텀 Dispatcher 또는 Compat override 로 메시지/검증 로직을 통합하세요.

---

## 변경 이력 (요약)

| 버전 | 주요 변경 |
|------|-----------|
| v1.7.x | 기본 DSL / Loader |
| v1.8.0 (예시) | sommand 엔트리, Compat, Args 강화, NumberStrings 추가, README 갱신 |

최신 실제 Git 태그를 확인 후 위 버전 표를 맞춰 주세요.

---

## 라이선스

MIT
