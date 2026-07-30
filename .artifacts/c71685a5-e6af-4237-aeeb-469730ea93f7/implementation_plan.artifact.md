# Implementation Plan - Simplify App Inference Architecture

Simplify the inference runners in `app:shared` by merging the `Runner` and `Handler` layers. This will be enabled by refactoring `LiteRTHandler` in the `tool` module to be more flexible.

## Proposed Changes

### [tool]

#### [MODIFY] [LiteRTHandler.kt](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/tool/src/commonMain/kotlin/io/github/kmplitert/tool/LiteRTHandler.kt)
- Remove `compiler` from the constructor.
- Add `protected abstract val compiler: LiteRTCompiler` as an abstract property.
- Update `runTask` and `close` to use this property.
- This allows classes that manage their own compiler lifecycle (like the app's Runners) to implement `LiteRTHandler` directly.

### [app:shared]

#### [MODIFY] [BaseLiteRTRunner.kt](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/app/shared/src/commonMain/kotlin/org/example/kmplitert/runner/BaseLiteRTRunner.kt)
- Make it inherit from `LiteRTHandler<I, O>`.
- Implement `compiler` property by returning the internal compiler instance (throwing if not initialized).
- Provide a default `run` implementation that calls `runTask(input)`.

#### [MODIFY] [MobileNetRunner.kt](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/app/shared/src/commonMain/kotlin/org/example/kmplitert/runner/MobileNetRunner.kt)
- Merge `MobileNetHandler` logic directly into `MobileNetRunner`.
- Override `preprocess` and `postprocess` directly.
- Remove the `classifier` property.

#### [MODIFY] [EfficientDetRunner.kt](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/app/shared/src/commonMain/kotlin/org/example/kmplitert/runner/EfficientDetRunner.kt)
- Merge logic from `EfficientDetHandler` into `EfficientDetRunner`.
- Remove the `detector` property.

#### [DELETE] [EfficientDetHandler.kt](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/app/shared/src/commonMain/kotlin/org/example/kmplitert/handler/EfficientDetHandler.kt)
- Remove the standalone handler file as its logic is now consolidated into the runner.

## Verification Plan

### Automated Tests
- Run `gradle :tool:assemble` to verify tool module changes.
- Run `gradle :app:shared:assemble` to verify consolidation.
- Run project tests.

### Manual Verification
- Check the code structure of `MobileNetRunner` and `EfficientDetRunner` to ensure they are now single-layer, flat implementations.
