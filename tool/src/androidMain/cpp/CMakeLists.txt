cmake_minimum_required(VERSION 3.18.1)

project("kmplitert_tool_native")

add_library(
    kmplitert_tool_native
    SHARED
    yuv_jni.cpp
    yuv_converter.cpp
)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
    kmplitert_tool_native
    ${log-lib}
    ${jnigraphics-lib}
)
