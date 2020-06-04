Transifex Team Member
online now


1:23 PM
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
also theexpression needs to be encapsulated in single quotes
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


