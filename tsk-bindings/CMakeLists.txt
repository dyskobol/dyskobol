cmake_minimum_required(VERSION 3.4)
project(tsk_bindings)

INCLUDE_DIRECTORIES(/usr/local/include/tsk)
INCLUDE_DIRECTORIES(/usr/lib/jvm/java-8-oracle/include/)
INCLUDE_DIRECTORIES(/usr/lib/jvm/java-8-oracle/include/linux)


set(CMAKE_CXX_STANDARD 11)
find_package(JNI)

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()

#add_executable(tsk_bindings main.cpp tsk_bindings.cpp tsk_bindings.h)
add_library(tsk_bindings SHARED src/tsk_bindings.cpp)
TARGET_LINK_LIBRARIES(tsk_bindings tsk)
