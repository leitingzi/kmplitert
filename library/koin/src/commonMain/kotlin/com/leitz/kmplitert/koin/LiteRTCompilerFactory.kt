package com.leitz.kmplitert.koin

import com.leitz.kmplitert.LiteRTCompiler

/**
 * 用于创建并初始化 [LiteRTCompiler] 实例。
 *
 * 由于 [LiteRTCompiler] 的初始化过程包含挂起操作，
 * 因此通过 Factory 统一封装创建逻辑。
 */
class LiteRTCompilerFactory {
    /**
     * 创建并初始化一个 [LiteRTCompiler]。
     *
     * @param filePath 模型文件路径。
     * @return 初始化完成后的编译器实例。
     */
    suspend fun create(filePath: String): LiteRTCompiler {
        val compiler = LiteRTCompiler()
        compiler.init(filePath)
        return compiler
    }
}