# Native `TFBuffer`

Native 平台的 `TFBuffer` 实现位于 `core/src/nativeMain/.../NativeTFBuffer.kt`。它通过 LiteRT TensorBuffer C API 访问张量内存。

## 读写机制

读写操作都会先锁定 LiteRT tensor buffer，操作完成后在 `finally` 中解锁：

- 写入时以写模式锁定，再将 Kotlin 数组复制到原生缓冲区。
- 读取时根据 LiteRT 报告的 packed size 创建 Kotlin 数组，以读模式锁定后复制数据。
- `BooleanArray` 通过逐个字节转换读写，非零字节会被解释为 `true`。
- `IntArray` / `FloatArray` 按 4 字节元素处理，`LongArray` 按 8 字节元素处理。

锁定失败或取得缓冲区大小失败时会抛出异常，而不是返回看似有效的空数据。

## 资源生命周期

`close()` 会销毁底层 LiteRT tensor buffer；重复关闭不会再次销毁。缓冲区关闭后，后续读写会因已关闭而失败。

遵循 compiler 的生命周期约定：推理完成后关闭 `LiteRTCompiler`，不要继续使用它所管理的缓冲区。若直接持有独立的 `TFBuffer`，也应在不再使用时关闭。

## 使用注意

- 写入的数据长度和类型应与模型张量及缓冲区要求相匹配。Native 实现执行内存复制，不应依赖它自动调整、填充或校验张量形状。
- 每次锁定后都会尝试解锁；不要在同一缓冲区并发执行彼此冲突的访问。
- 读取操作是挂起函数，应在适当的 coroutine 上下文中调用。
- 这些实现细节针对 Native 后端；其他平台可能采用不同的存储和复制策略。
