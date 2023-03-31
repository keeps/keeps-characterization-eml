> :warning: **This project is no longer maintained.**  
> For tools related to RODA, please look at https://market.roda-community.org


keeps-characterization-eml
==========================

Characterization tool for eml files, made by KEEP SOLUTIONS.


## Build & Use

To build the application simply clone the project and execute the following Maven command: `mvn clean package`  
The binary will appear at `target/eml-characterization-tool-1.0-SNAPSHOT-jar-with-dependencies.jar`

To see the usage options execute the command:

```bash
$ java -jar target/eml-characterization-tool-1.0-SNAPSHOT-jar-with-dependencies.jar -h
usage: java -jar [jarFile]
 -f <arg> file to analyze
 -h       print this message
 -v       print this tool version
```

## Tool Output Example
```bash
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<emlCharacterizationResult>
    <validationInfo>
        <valid>true</valid>
    </validationInfo>
    <features>
        <item>
            <key>to</key>
            <value>[someone@somewhere.com]</value>
        </item>
        <item>
            <key>subject</key>
            <value>Welcome</value>
        </item>
        <item>
            <key>textBody</key>
            <value>Dear Friend,&#xD;
                &#xD;
                Welcome to file.fyicenter.com!&#xD;
                &#xD;
                Sincerely,&#xD;
                FYIcenter.com Team&#xD;
            </value>
        </item>
        <item>
            <key>from</key>
            <value>[file@fyicenter.com]</value>
        </item>
        <item>
            <key>htmlBody</key>
            <value></value>
        </item>
    </features>
</emlCharacterizationResult>


```

## License

This software is available under the [LGPL version 3 license](LICENSE).

