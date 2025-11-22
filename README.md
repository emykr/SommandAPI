[![](https://jitpack.io/v/emykr/SommandAPI.svg)](https://jitpack.io/#emykr/SommandAPI)

# SommandAPI

SommandAPI 는 Bukkit/Paper 환경에서 선언형 DSL 로 명령 트리를 정의하고 자동으로 권한/등록/실행을 처리하는 경량 라이브러리입니다.

## 최근 변경(정리)
- DSL 빌더 `SommandBuilder` 단순화 (명령 트리 정의 책임만)
- 인자 팩토리 `Args` 추가 (입력 유효성 검증 포함)
- 숫자 포맷/출력 유틸 `NumberStrings` 추가
- 버전 호환 구조(`compat` 패키지: `BaseCompat`, 버전별 `VersionCompat`, `CompatResolver`) 도입 — 현재 DSL과 자동 연결되지는 않으며 선택적으로 사용할 수 있음

> 주의: sommand(plugin) { ... } 형태의 최상위 DSL 엔트리 함수가 레포에 아직 없다면 기존 `SommandLoader(plugin).load { ... }` 또는 레거시 호출 방식을 계속 사용하십시오. 해당 함수 추가 후 README 를 다시 갱신하세요.

---

## 설치 (Installation)

최신 태그는 JitPack 배지를 확인하세요: [SommandAPI](https://jitpack.io/#emykr/SommandAPI)

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.emykr:SommandAPI:v1.7.3")
}
```

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.emykr:SommandAPI:v1.7.3"
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
    <version>v1.7.3</version>
  </dependency>
</dependencies>
```

---

## 빠른 시작 (Quick Start)

`JavaPlugin` 안에서 명령 정의:

```kotlin
class ExamplePlugin : JavaPlugin() {
    override fun onEnable() {
        // 실제 레포 기준으로 맞는 초기화/로더 호출 사용
        SommandLoader(this).load {
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

---

## DSL 구조

```kotlin
command(vararg aliases, description = "...", permission = "node") {
    executes { /* /<root> */ }

    literal("sub") { /* /<root> sub */ }

    literalExec("info", description = "Show info") { /* /<root> info */ }

    argument(SomeArgument("value")) {
        literalExec("detail") { /* /<root> <value> detail */ }
    }

    argumentExec(SomeArgument("amount")) { /* /<root> <amount> */ }

    group("admin") {
        literalExec("reload", permission = "plugin.reload") { /* /<root> admin reload */ }
    }
}
```

요약:
- `literal()` : 고정 토큰 분기
- `literalExec()` : 리터럴 + 즉시 실행
- `argument()` : 파싱 가능한 토큰 수용, 후속 자식 가능
- `argumentExec()` : 인자 + 즉시 실행
- `executes {}` : 현재 노드 직접 실행
- `group()` : 리터럴 sugar
- `simple()` : 최소 정의 (루트 실행 전용)

---

## 권한(Permission) 처리

1. 루트 및 자식 노드 정의 시 `permission` 문자열 지정 가능
2. 로더/디스패처가 실행 과정에서 노드 접근 전 `permission` 검사
3. 기본 실패 메시지 커스터마이즈 필요 시 Dispatcher 또는 Compat 활용

권장:
- 핵심 동작 노드만 퍼미션 부여
- 패턴: `plugin.domain.action` (예: `myplugin.user.delete`)

---

## Args 인자 팩토리

```kotlin
argumentExec(Args.int("amount", min = 1, max = 64)) { ... }
argumentExec(Args.enum<MyMode>("mode")) { ... }
argumentExec(Args.player("target")) { ... }
argumentExec(Args.greedyString("message")) { ... }
```

내부 유효성:
- 이름 공백/빈 문자열 방지
- 숫자 min/max 역전 시 예외
- enum 제네릭 정확성 유지

커스텀 인자 예:

```kotlin
class IntArgument(
    private val name: String,
    private val min: Int? = null,
    private val max: Int? = null
) : CommandArgument<Int> {
    override val id: String = name
    override fun parse(input: String, ctx: CommandArgument.Context): CommandArgument.Result<Int> {
        val v = input.toIntOrNull() ?: return CommandArgument.Result.failure("Expected integer for '$name'.")
        if (min != null && v < min) return CommandArgument.Result.failure("Value <$min")
        if (max != null && v > max) return CommandArgument.Result.failure("Value >$max")
        return CommandArgument.Result.success(v)
    }
    override fun suggestions(prefix: String, ctx: CommandArgument.Context): List<String> =
        if (prefix.all { it.isDigit() }) listOf(prefix) else emptyList()
}
```

---

## 실행 컨텍스트 (ExecutionScope)

실제 구현에 따라 달라질 수 있는 예상 기능:

| 구성 | 설명 |
|------|------|
| `sender` | 명령 발신자 (`CommandSender`) |
| `arg<T>(name)` | 파싱된 인자 조회 |
| `reply(message)` | 간단 메시지 전송 |
| `fail(message)` | 실행 중단 + 실패 피드백 |

> 실제 `SommandNode.ExecutionScope` 구현 명세를 확인 후 README 업데이트 권장.

---

## 버전 호환 (Compat) - 선택적 확장

`compat` 패키지:
- `BaseCompat` : 메시지/디스패처 커스터마이즈용 추상
- `VersionCompat` (예: `v1_21_4`, `v1_21_9`, `v1_21_10`)
- `CompatResolver` : 서버 버전 문자열 감지 후 적절한 Compat 제공

사용 예 (수동):

```kotlin
val compat = CompatResolver.resolve()
val dispatcher = compat.createDispatcher()
// dispatcher 를 커맨드 등록/실행 체계와 통합하려면 추가 연결 코드 필요
```

> 현재 DSL 생성 시 자동으로 compat 주입되지 않는다면 README 를 따라 별도 연결 로직을 작성하세요.

---

## 숫자 포맷 유틸 (NumberStrings)

숫자 출력 표준화:

```kotlin
val coins: Int? = 12345
NumberStrings.withLocale(coins)                    // "12,345"
NumberStrings.withPattern(coins, "#,##0")          // "12,345"
NumberStrings.custom(coins) { n -> "${n} pts" }    // "12345 pts"
NumberStrings.template("You have {c} coins.", "{c}", coins)
```

Bukkit 객체 메시지:

```kotlin
sender.sendMessage(NumberStrings.forPlayerValue(player, 10, label = "lives"))
```

색 코드 처리:

```kotlin
val colored = NumberStrings.ampersandToSection("&aValue: 10")
```

---

## 예: 하이브리드 명령

```kotlin
SommandLoader(this).load {
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

## 스타일 가이드

- 첫 root alias: 짧고 직관적 (`user`, `greet`)
- Description: 사용자 도움말/자동 문서화 고려하여 주요 노드에 작성
- Permission: 최소 권한만 (루트/중요 실행)
- 인자 이름: 짧고 의미 명확 (`userName` → `name`)

---

## 표준 에러/실패 메시지 (예시)

| 상황 | 메시지 (기본) |
|------|---------------|
| 알 수 없는 토큰 | Unknown subcommand. Use /<root> help |
| 인자 파싱 실패 | Failed to parse argument: <detail> |
| 권한 부족 | You do not have permission. |
| 실행 중 예외 | An internal error occurred while attempting to perform this command. |

> 커스터마이즈는 Compat 또는 Dispatcher 교체로 가능.

---

## Permission 자동 등록 (구현 확인 필요)

일반 흐름(예상):
1. 등록된 루트 노드 순회
2. 노드별 permission 수집
3. Bukkit `PluginManager` 에서 중복 여부 검사 후 동적 Permission 생성
4. `PermissionDefault.OP` 기본값 (구체 구현 확인 후 README 보강)

---

## Dispatcher 커스터마이즈

```kotlin
class LoggingDispatcher : SommandDispatcher {
    override fun dispatch(source: SommandSource, label: String, tokens: List<String>, root: SommandNode): Boolean {
        // 기본 트리 탐색 + 추가 로깅
        return true
    }

    override fun suggest(source: SommandSource, label: String, tokens: List<String>, root: SommandNode): List<String> {
        return emptyList()
    }
}
```

---

## FAQ

**Q. plugin.yml 에 명령을 등록해야 하나요?**  
A. 동적 등록이 동작하면 필수는 아닙니다.

**Q. greedy 인자 뒤에 다른 노드를 둘 수 있나요?**  
A. 일반적으로 greedy 인자는 나머지 토큰을 모두 소비하므로 마지막에 두는 것을 권장합니다.

**Q. 모든 메시지를 커스터마이즈하고 싶습니다.**  
A. Compat 구현 확장 또는 Dispatcher 교체로 가능.

---

## 변경 이력 (요약)

| 버전 | 주요 변경(요약) |
|------|----------------|
| v1.7.3 | DSL 단순화, Args/NumberStrings 추가, compat 기반 구조 도입(선택) |

> 실제 태그/릴리스 노트 기준으로 표를 주기적으로 갱신하세요.

---

## 라이선스

MIT (다른 라이선스라면 교체)

---

## 향후 문서 보강 TODO (필요 시)
- SommandNode.ExecutionScope 실제 메서드 목록 확정 후 표 갱신
- Permission 자동 등록 실제 구현 코드 인용
- sommand(plugin) 최상위 함수 도입 후 사용 섹션 교체
- Compat 자동 연결 샘플 적용
