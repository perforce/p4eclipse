# P4Eclipse - Perforce Eclipse Plugin

P4Eclipse is an Eclipse plugin that provides integration with Perforce version control system, enabling developers to work with Perforce repositories directly from within the Eclipse IDE.

## Prerequisites

- Java 17 (OpenJDK recommended)
- Apache Maven 3.9.5+

## Build on Linux
    1. Clone the repository
        ```git clone https://github.com/perforce/p4eclipse.git
            cd p4eclipse
        ```
    2. Run the build 
        ./build.sh 2025.1.4
   
    **Expected output:**
        ```
        3.7/build/p4eclipse_updatesite/
        ├── p4eclipse-updatesite-2025.1.1.zip 
        └── site.xml                  
        ```

    **Examples to run build.sh:**
        ```
        ./build.sh 12.0.0-SNAPSHOT
        ./build.sh 2025.1.1 --target p4e-428 --p2repo https://download.eclipse.org/releases/2023-06/
        ./build.sh 12.0.0-SNAPSHOT --skip-tests
        ./build.sh 2025.1.1 --target p4e-428 --p2repo https://example.com/ --skip-tests
        ```

*** To buils on windows use build.bat **

### Build Targets

Available Eclipse target platforms (extensive list):

- `p4e-432` - Eclipse 4.32 (https://download.eclipse.org/releases/2024-06/)
- `p4e-431` - Eclipse 4.31 (https://download.eclipse.org/releases/2024-03/)
- `p4e-430` - Eclipse 4.30 (https://download.eclipse.org/releases/2023-12/)
- `p4e-429` - Eclipse 4.29 (https://download.eclipse.org/releases/2023-09/)
- `p4e-428` - Eclipse 4.28 (https://download.eclipse.org/releases/2023-06) - **Default**


### Installation

To install in Eclipse:

1. Help → Install New Software
2. Add → Archive → Select the ZIP file
3. Select "Perforce Eclipse Plugin"
4. Complete installation wizard
5. Restart Eclipse


### Project Structure

```
p4eclipse/
├── 3.7/                          # Main Eclipse 3.7+ support
│   ├── build/                    # Build configuration
│   │   ├── p4eclipse_parent/     # Parent POM and build scripts
│   │   ├── p4eclipse_repo/       # Update site generation
│   │   └── p4eclipse_target/     # Target platform definitions
│   ├── features/                 # Eclipse features
│   │   ├── com.perforce.team.feature/
│   │   ├── com.perforce.team.java.feature/
│   │   └── ...
│   └── plugins/                  # Eclipse plugins
│       ├── com.perforce.team.core/
│       ├── com.perforce.team.ui/
│       └── ...
├── build.sh                     # Main build script
├── build.bat                    # Windows build script
└── README.md                    # This file

## License

See `LICENSE.txt` for license information.
