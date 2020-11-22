# Transifex evaluation

## Brazilian Portuguese translation [#237](https://github.com/sjPlot/Zettelkasten/issues/237)

This section describes the configuration for the translation project from English (en_GB) to Brazilian Portuguese (pt_BR). The next section "Project Integrations" describes in particular the learning experiences made regarding the YAML configuration of the file synchronization between the GitHub repository and the Transifex project(s).

The special thing about the pt_BR project is that the source language should be English. Until now, however, the source language has been German. With Transifex we could probably work on several projects in parallel and configure a different source language for each project if needed. (German for the overall project Zettelkasten and English for the pt_BR project)

### YAML config

It seems to boil down to working with the filter type file instead of filter type directory. So, we list each file individually in the YAML configuration of the respective project.

Copy and paste the following [YAML configuration](pt_BR/pt_BR.yaml) to the Update Link Settings, 
Select Files dialog of the pt_BR project at Transifex named "Brazilian Portuguese translation #237":

```yaml
filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/AboutBox_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CAutoKorrekturEdit_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CAutoKorrekturEdit_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CBiggerEditField_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CBiggerEditField_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopExport_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopExport_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopMultipleExport_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopMultipleExport_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CErrorLog_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CErrorLog_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CExportEntries_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CExportEntries_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CExport_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CExport_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CFilterSearch_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CFilterSearch_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CFindReplaceDialog_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CFindReplaceDialog_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CFontChooser_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CFontChooser_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CFormEditor_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CFormEditor_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CHighlightSearchSettings_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CHighlightSearchSettings_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CImportBibTex_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CImportBibTex_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CImport_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CImport_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CInformation_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CInformation_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CInsertHyperlink_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CInsertHyperlink_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CInsertManualLink_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CInsertManualLink_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CInsertTable_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CInsertTable_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CModifyDesktopEntry_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CModifyDesktopEntry_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CNewBookmark_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CNewBookmark_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CPdfExportSettings_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CPdfExportSettings_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CRateEntry_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CRateEntry_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CReplaceDialog_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CReplaceDialog_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CSearchDlg_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CSearchDlg_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CSetBibKey_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CSetBibKey_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CSettingsDlg_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CSettingsDlg_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CShowMultipleDesktopOccurences_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CShowMultipleDesktopOccurences_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CStenoEdit_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CStenoEdit_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CSynonymsEdit_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CSynonymsEdit_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CTexExportSettings_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CTexExportSettings_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CUpdateInfoBox_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CUpdateInfoBox_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/DesktopFrame_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/DesktopFrame_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/EditorFrame_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/NewEntryFrame_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/SearchResultsFrame_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/SearchResultsFrame_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/ToolbarIcons_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/ToolbarIcons_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/ZettelkastenApp_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/ZettelkastenApp_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/ZettelkastenView_en_GB.properties
    source_language: en
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/ZettelkastenView_<lang>.properties'

```
## Project Integrations
### GitHub
#### Edit Settings
Update Link Settings > Select Files > Select files to synchronize
GITHUB
#### Mapping
NOTE: You can map files in a GitHub repository/directory to resources in Transifex. 
* `filter_type: dir` or
* `filter_type: file` 

The first attempt looked like this:
```yaml
filters:
  - filter_type: dir
    # all supported i18n types: https://docs.transifex.com/formats
    file_format: PROPERTIES
    source_file_extension: properties
    source_language: de
    source_file_dir: src/main/resources/de/danielluedecke/zettelkasten/resources/
    # path expression to translation files, must contain <lang> placeholder
    translation_files_expression: 'translations/<lang>/'
```
[More Examples of YAML Configurations](https://docs.transifex.com/transifex-github-integrations/github-tx-ui#more-examples-of-yaml-configurations-) ?

In order to maintain our structure in GitHub we need to use the file dir type and specify each file name in the yaml config file:
```yaml
filters:
  - filter_type: dir
    # all supported i18n types: https://docs.transifex.com/formats
    file_format: PROPERTIES
    source_file_extension: properties
    source_language: de
    source_file_dir: src/main/resources/de/danielluedecke/zettelkasten/resources/
    # path expression to translation files, must contain <lang> placeholder
    translation_files_expression: 'translations/<lang>/'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CAutoKorrekturEdit.properties
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CAutoKorrekturEdit_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CBiggerEditField.properties
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CBiggerEditField_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopDisplayItems.properties
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopDisplayItem_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopExport.properties 
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopExport_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopMultipleExport.properties
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CDesktopMultipleExport_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CErrorLog.properties
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CErrorLog_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CExport.properties
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CExport_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/CExportEntries.properties
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/CExportEntries_<lang>.properties'

filters:
  - filter_type: file
    file_format: PROPERTIES
    source_file: src/main/resources/de/danielluedecke/zettelkasten/resources/
    source_language: de
    translation_files_expression: 'src/main/resources/de/danielluedecke/zettelkasten/resources/ _<lang>.properties'
```
## Languages 
https://www.transifex.com/zettelkasten/zettelkasten/languages/

Edit languages

### Current
* English (United Kingdom) (en_GB)
* Portugeuse (Brazil) (pt_BR)
* Spanish (Spain) (es_ES)

NOTE: The focus should be on the new language pt_BR at the moment. 
Keyword here is the CLI Client and:
````shell
$ tx pull -l pt_BR
```` 

## CLI Client
[Configuring one local file (mapping)](https://docs.transifex.com/client/config#configuring-one-local-file-(mapping)) > Resources
````shell
$ tx config mapping -r <project_slug.resource_slug> --source-lang <lang_code> \
--type <i18n_type> [--source-file <file>] --expression '<path_expression>'
````

To be able to use the CLI client, the `.tx/config` file must be configured. 
We need the Slug information from https://www.transifex.com/zettelkasten/zettelkasten/content/ > Resources

## Edit resource settings
### Slug (example)
src-main-resources-de-danielluedecke-zettelkasten-resources-cautokorrekturedit-properties--feature-transifex
### Name (example)
src..resources/CAutoKorrekturEdit.properties (feature/Transifex)

## Chat with Transifex Team Member
2020-06-04 1:23 PM
Tex
Hi! 👋 I'm Tex, the Transifex Bot!

I hope you and your loved ones are doing well given the current circumstances.

What brought you to Transifex today? Is there anything I can help you with? 

2:59 PM
I'm already using Transifex, I need support!
Tex
Let's get you the help you need!

Which of these best describes you?

Customer
Tex
Thank you for being one of our valued customers!

What can we do for you today?

I need help with our YAML config for Java Properties files.

Tex
Thanks for sharing that information.

What's the best email address for us to follow-up with you and provide assistance?


Hello. Could you please provide me with a link to your project so that I can verify the configuration?

Link to our project: https://www.transifex.com/zettelkasten/zettelkasten

There is an issue with the translation files expression:   translation_files_expression: src/main/resources/de/danielluedecke/zettelkasten/resources/{filename}_<lang>.properties. You can't use the placeholder {filename}. The integration automatically assigns the same filename in the translation file as the sourcefile
also the expression needs to be encapsulated in single quotes
the <lang> keyword can only be used as a directory name

Many thanks for the quick answer! Will try to fix that YAML config later. I am also not yet aware of the relationship between the individual files in the Java resource bundles. In our first test project @ https://www.transifex.com/Elmari/zettelkasten-elmari we manually uploaded 39 resources (German (en) [source language]. In the new, second project with GitHub integration, to which my question regarding the YAML config refers, there are now 102 resources. What do I have to do to keep the existing files in en_GB and es_ES and their contents from our GitHub repro?

Just to confirm are you asking how do existing translations get transferred to transifex from github

Yes, we have existing translations in the Java Resource Bundles, i.e. Java Properties files.
https://github.com/Zettelkasten-Team/Zettelkasten.git
I expected that I could somehow map the existing *_en_GB.properties and *_es_ES.properties files to the DE master *.properties files when importing from GitHub to Transifex.

the first time the integration is run, the integration will download the translation files into transifex. Just configure the languages in your project.

translation_files_expression: ist now 'src/main/resources/de/danielluedecke/zettelkasten/resources/'

I don't understand why each properties file was imported individually from GitHub to Transifex.

the translation files expression requires the lang keyword as a directory

The overview at https://www.transifex.com/zettelkasten/zettelkasten/content/ probably confuses me a bit.
For example, I would like to have only one resource for CAutoKorrekturEdit.properties (in the master language DE) instead of the current 3 resources.

let me check

You wrote: "the translation files expression requires the lang keyword as a directory"

Does that mean we would have to change/adapt the directory structure in our source code repo?

the translation files in github need to be in a separate location in github. The integration pushes everything including whats in subdirectories

you would have to change/adapt your repo

Yes, the translation files in github are all in a seprate location, i.e. src/main/resources/de/danielluedecke/zettelkasten/resources

in order to maintain your structure in github you need to use the file dir type and specify each file name in the yaml config file

You wrote: "you would have to change/adapt your repo"

Does this mean we would have to move the existing *_en_GB.properties and *_es_ES.properties files to another subdirectory before importing them from GitHub to Transifex?
Where can I find documentation or an example for using the file dir type ?
https://docs.transifex.com/transifex-github-integrations/github-tx-ui writes about filter_type: dir

https://docs.transifex.com/transifex-github-integrations/github-tx-ui#more-examples-of-yaml-configurations-

Thanks for the advice. I think I've seen these examples before.

Is there anything else I can help you with?

Yeah, thanks for your help so far, is there also an example how to use the file dir type and specify each file name in the yaml config file? 

And then how do we get the CLI tool to run?
$ tx config mapping-remote https://www.transifex.com/zettelkasten/zettelkasten

By the way, my e-mail address is ralf.barkow@web.de 

I have to go now. 

Many thanks for your support!!!

Delivered
5:17 PM
Type your message…


