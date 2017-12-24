# JMeter test tools
This project contains few tools for JMeter tests

# JMeter test generator
## Build
`mvn clean package`
## Run
#### Help
`./target/jmeter-test-generator.jar --help`
#### Generate test from default template
`./target/jmeter-test-generator.jar -s /path/to/sql-file.sql -j /path/to/output.jmx`
#### Generate test from custom template
`./target/jmeter-test-generator.jar -s /path/to/sql-file.sql -j /path/to/output.jmx -t /path/to/template.xml`
#### Generate many tests from directory with .sql files
`./generate-all.sh /path/to/dir [/path/to/template.xml]`
#### Generate many responses for tests from directory with .jmx files
`./generate-all-responses.sh /path/to/dir/with/jmx/files /path/to/output/dir`
## Documentation
[Velocity User Guide](http://velocity.apache.org/engine/releases/velocity-1.7/user-guide.html)

[Velocity Template Language](http://velocity.apache.org/engine/releases/velocity-1.7/vtl-reference-guide.html)

# Tool that allow add expected results to the .jmx file
## Build
`mvn package -P add-assertions`
## Run
#### Help
`./target/add-assertions.jar --help`
#### Add assertions
`./target/add-assertions.jar -i /path/to/input-file.jmx -o /path/to/output-file.jmx -e /path/to/expected-results.xml`
