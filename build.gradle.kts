plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.firefly"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Thymeleaf extras
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Caffeine cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // OpenAPI / SpringDoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Micrometer Prometheus
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Lombok (optional convenience)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

springBoot {
    mainClass.set("com.firefly.FireflyApplication")
}