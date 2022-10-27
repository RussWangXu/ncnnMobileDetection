project(ncnnDetection)

cmake_minimum_required(VERSION 3.4.1)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20220413-local-build/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(ncnnDetection SHARED ncnnDetection.cpp)

target_link_libraries(ncnnDetection
        ncnn
        jnigraphics
)
