    fabric 모듈 사용법

이 모듈은 Paper/Bukkit용 SommandDSL과 동일한 DSL 구조(sommand)를 Fabric 환경에서도 사용할 수 있도록 제공하는 헬퍼입니다.

간단 사용법

1. 프로젝트의 Fabric mod 초기화 코드에서 sommandFabric { ... } 를 호출하여 커맨드를 선언하세요.
2. SommandDSL은 공용 CommandRegistry에 RootNode들을 등록합니다. Fabric에서 실제로 명령을 등록하려면 서버 시작 시 CommandRegistry.allDistinct()를 읽어 Brigadier 등 Fabric API를 사용해 변환/등록하세요.

예시

```kotlin
// ModInitializer 또는 서버 시작 로직
sommandFabric {
    command("hello") {
        executes {
            source.send("Hello from Fabric DSL")
        }
    }
}

// 이후 Fabric 쪽에서 CommandRegistry.allDistinct()를 읽어 Brigadier 쪽으로 변환하여 등록
```

주의 사항

- Fabric용 실제 커맨드 등록(Brigadier로 변환)은 이 모듈에서 자동으로 수행하지 않습니다. 서버/공식 Fabric API 종속성을 추가한 뒤 변환 로직을 구현해야 합니다.
- root 프로젝트의 SommandAPI 코드는 공용 데이터 모델(RootNode, ArgumentNode 등)을 사용합니다.
