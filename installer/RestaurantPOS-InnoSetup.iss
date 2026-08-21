; Inno Setup Script for Restaurant POS Windows Installer
; Compiles into RestaurantPOS-Setup.exe

#define MyAppName "Restaurant POS"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Restaurant POS Enterprise"
#define MyAppURL "https://github.com/alxxrzfyr/restaurant-pos"
#define MyAppExeName "RestaurantPOS.exe"

[Setup]
AppId={{D38F26D7-CBF6-4E19-94A4-DF1FF84566CB}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir=..\dist
OutputBaseFilename=RestaurantPOS-Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "..\target\app.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\install.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\sample-images\*"; DestDir: "{app}\sample-images"; Flags: ignoreversion recursesubdirs createallsubdirs; Tasks: ; Languages: 

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "javaw.exe"; Parameters: "-jar ""{app}\app.jar"""; WorkingDir: "{app}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "javaw.exe"; Parameters: "-jar ""{app}\app.jar"""; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "javaw.exe"; Parameters: "-jar ""{app}\app.jar"""; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: shellexec postinstall nowait skipifsilent
