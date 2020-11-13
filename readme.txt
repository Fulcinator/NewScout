SCOUT - A FRAMEWORK FOR AUGMENTED TESTING


SYSTEM REQUIREMENTS

Scout is implemented in Java and needs a Java Runtime Environment (JRE).
Scout can run in any Java supported operating system, like Windows, Linux, Mac och Unix.


START SCOUT

Double-click the "Scout.jar" or "Scout.exe" file to start Scout. You may also launch Scout using the command:

java -jar Scout.jar

Note: A Java Runtime is not required when starting Scout using the "Scout.exe" file on a Windows machine.


CUSTOMIZING SCOUT

Plugins, binaries and source code, can be found in the "plugin" folder.
Plugins placed in the "plugin" folder are automatically loaded by Scout when starting.
View the doncumentation in the "javadoc" folder for details how to use the Scout API.


RELEASE NOTES

Version 1.9:
1. Toolbar replaced by AugmentToolbar plugin.
2. Faster click (does not wait for a double-click).
3. Pause and Resume have been removed (can be restored by editing the AugmentToolbar plugin).
4. Go Home button in the toolbar.
5. The Auto button clears the coverage if 100%.

Version 1.8:
1. getLastCapture method in PluginController.
2. processCapture method available for modifying the capture.
3. WidgetMetadata plugin that augments the metadata of a widget.
4. Gray plugin that produces a grayscale image from the capture.

Version 1.7:
1. SessionReports and IssueReports use a filename compatible with Ubuntu.
2. ManualInstruction overwrites the current instruction, if needed.
3. Plugins may augment the default background image.
4. Setup program for Scout.

Version 1.6:
1. Improved comments in the AugmentState and SeleniumPlugin plugins.

Version 1.5:
1. Improved AugmentState plugin.
2. EasterEgg plugin.

Version 1.4:
1. SuggestClones plugin now support type, select and gohome widgets.
2. HelloWorld example plugin.

Version 1.3:
1. Enable/disable plugins from Scout.
2. Drop-down for reports.
3. Support for bookmarks.
4. Improved augmentation.
5. Manual instruction plugin.
6. Support for password fields in the Selenium plugin.

Version 1.2:
1. Improved handling of scroll wheel.
2. Fixed resize window problem.

Version 1.1:
1. Improved strategy for selecting the recoomended widget.
2. Suggestions are no longer recommended.
3. Improved and simplified API.
4. Arrows indicate multiple visible widgets.

Version 1.0:
1. Expressions for validating checks.
2. Modify expressions using the keyboard.
3. Cookies stored per state.

Version 0.9:
1. Improved and simplified handling of suggestions.
2. Checks and clicks in frames.
3. Improved drag and drop.
4. Improved selection of widgets.
5. Fixed defect in the autopilot plugin.
6. Add a comment or report an issue using the enter key.

Version 0.8:
1. Improved detection of checks (Selenium plugin).

Version 0.7:
1. Improved widget recognition (Selenium plugin).
2. More options in start session dialog.

Version 0.6:
1. Support for reports.
2. Plugin that report all sessions in the current product version.
3. Plugin that report all issues.

Version 0.5:
1. Report issues.
2. Navigate to issues.
3. Help instructions.

Version 0.4:
1. All drawing is performed by plugins.
2. Actions are created by Scout and performed by plugins.
3. Faster typing of text.
4. No popups.

Version 0.3:
1. Type a text directly using the keyboard.
2. More stable autopilot.
3. Suggestion plugins only give the suggestions one time.

Version 0.2:
1. Support for suggestions.
2. Boundry value suggestions plugin.
3. Plugin that suggests clones.
4. Random mission plugin.
5. Possible to enter many values into the same text input field.

Version 0.1:
1. Contains basic features for recording a session and auto executing a recorded session.
2. Support buttons and text input fields but not drop-downs and lists.
