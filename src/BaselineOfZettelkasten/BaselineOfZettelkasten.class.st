Class {
	#name : #BaselineOfZettelkasten,
	#superclass : #BaselineOf,
	#category : #BaselineOfZettelkasten
}

{ #category : #accessing }
BaselineOfZettelkasten class >> loadLepiter [
	| currentProperties |
	currentProperties := LeDatabasesRegistry defaultLogicalDatabase properties.
	currentProperties
		addRegisteredDirectory: FileLocator imageDirectory
			/ 'pharo-local'
			/ 'iceberg'
			/ 'RalfBarkow'
			/ 'Zettelkasten'
			/ 'lepiter'.
	
	LeDatabasesRegistry default defaultLogicalDatabase reload.
]

{ #category : #accessing }
BaselineOfZettelkasten >> baseline: spec [
	<baseline>
	spec for: #common do: [
		spec
			package: 'Zettelkasten' ]
]
