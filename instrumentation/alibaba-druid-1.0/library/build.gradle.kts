plugins {
  id("otel.library-instrumentation")
  id("otel.nullaway-conventions")
}

dependencies {
  library("com.alibaba:druid:1.0.0")

  testImplementation(project(":instrumentation:alibaba-druid-1.0:testing"))
}
