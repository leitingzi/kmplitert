# Add `build-logic` to Consolidate Duplicate Build Code

This plan aims to remove duplication between `core` and `tool` by extracting shared Gradle logic into a dedicated `build-logic` module.

## User Review Required

> [!NOTE]
> I will use the directory name `build-logic` instead of `build` to avoid conflict with Gradle's default `build/` output directory. This will be implemented as an "Included Build", which is the modern Gradle standard for sharing build logic.

## Proposed Changes

### 1. Create `build-logic` Module

#### [NEW] [build-logic/settings.gradle.kts](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/build-logic/settings.gradle.kts)
- Initialize the build logic project.

#### [NEW] [build-logic/build.gradle.kts](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/build-logic/build.gradle.kts)
- Configure the `kotlin-dsl` plugin to allow writing build logic in Kotlin.

#### [NEW] [build-logic/src/main/kotlin/kmplitert.native-conventions.gradle.kts](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/build-logic/src/main/kotlin/kmplitert.native-conventions.gradle.kts)
- Extract the `KonanTarget` extensions (`isApple`, `isLinux`, `libDir`).
- Extract the `bundle` task registration logic.
- Extract the `KotlinNativeTest` environment setup logic.

### 2. Update Root Project

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/settings.gradle.kts)
- Add `includeBuild("build-logic")`.

### 3. Refactor Module Gradle Files

#### [MODIFY] [core/build.gradle.kts](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/core/build.gradle.kts)
- Apply the new `kmplitert.native-conventions` plugin.
- Remove duplicate extensions and task logic.

#### [MODIFY] [tool/build.gradle.kts](file:///C:/Users/v_yebtang/IdeaProjects/kmplitert/tool/build.gradle.kts)
- Apply the new `kmplitert.native-conventions` plugin.
- Remove duplicate extensions and task logic.

## Verification Plan

### Automated Tests
- Run `./gradlew :core:assemble`
- Run `./gradlew :tool:assemble`
- Verify that the `bundle` tasks are still created and executed correctly.

### Manual Verification
- Perform a Gradle Sync to ensure the new `build-logic` is correctly integrated.
- Check that the native libraries are still correctly bundled in the output frameworks/binaries.
