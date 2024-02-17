plugins {
    application
    eclipse
}

repositories {
    mavenCentral()
}

java.sourceCompatibility = JavaVersion.VERSION_17

val osgi_platform = "cocoa.macosx.aarch64"
val swt_version = "3.124.100"

dependencies {
    implementation("commons-logging:commons-logging:1.2")
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
    implementation("org.eclipse.platform:org.eclipse.swt.$osgi_platform:$swt_version")
    
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("org.eclipse.platform:org.eclipse.swt.\${osgi.platform}")).using(module("org.eclipse.platform:org.eclipse.swt.$osgi_platform:$swt_version"))
        }
    }
}

application {
    mainClass.set("org.ellab.magman.SwtMain")
    applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
}

java.sourceSets["main"].java {
    srcDir("src/main/java")
}

java.sourceSets["test"].java {
    srcDir("src/main/test")
}
