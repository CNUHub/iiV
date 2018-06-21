#!/bin/csh
set version=119
# remember to update multiJarMainClass with the new version
#
set classpath=classes:bsh:Acme:.
set classdir="`pwd`/classes"
set coptions="-d ${classdir} -classpath $classpath"
set docoptions="-public -classpath $classpath -d iiV${version}api -doctitle iiV${version} -header iiV${version} -footer iiV${version} -linkoffline http://java.sun.com/j2se/1.4/docs/api j2se1.4.2packages -linkoffline http://www.beanshell.org/javadoc bshpackages"
set jarcmd=jar
set jardir=`pwd`

# start building all in one files
set jarsrcfile=${jardir}/iiV${version}src.jar
set jarclassfile=${jardir}/iiV${version}.jar
${jarcmd} cvfm ${jarclassfile} allInOneMainClass LICENSE changes
${jarcmd} cvf ${jarsrcfile} allInOneMainClass multiJarMainClass LICENSE changes bldiiv.csh

set compilefiles=iiv/iiV.java
#
set compilefiles=(${compilefiles} iiv/CNUViewer.java)
set compilefiles=(${compilefiles} iiv/CNUViewerActions.java)
#
set compilefiles=(${compilefiles} iiv/data/AffineCoordinateMap.java)
set compilefiles=(${compilefiles} iiv/data/AffineDataSlicer.java)
set compilefiles=(${compilefiles} iiv/data/AffineMatrix.java)
set compilefiles=(${compilefiles} iiv/data/CNUConversionTypes.java)
set compilefiles=(${compilefiles} iiv/data/CNUData.java)
set compilefiles=(${compilefiles} iiv/data/CNUDataSlicer.java)
set compilefiles=(${compilefiles} iiv/data/CNUDimensions.java)
set compilefiles=(${compilefiles} iiv/data/CNUScale.java)
set compilefiles=(${compilefiles} iiv/data/CNUTypesConstants.java)
set compilefiles=(${compilefiles} iiv/data/CNUTypes.java)
set compilefiles=(${compilefiles} iiv/data/CoordinateMap.java)
set compilefiles=(${compilefiles} iiv/data/CoordinateMappable.java)
set compilefiles=(${compilefiles} iiv/data/LinearCoordinateMap.java)
set compilefiles=(${compilefiles} iiv/data/NiftiQFormCoordinateMap.java)
set compilefiles=(${compilefiles} iiv/data/NiftiSFormCoordinateMap.java)
set compilefiles=(${compilefiles} iiv/data/PrimaryOrthoDataSlicer.java)
set compilefiles=(${compilefiles} iiv/data/ScaleInterface.java)
set compilefiles=(${compilefiles} iiv/data/XYZDouble.java)
set jarfiles=(${compilefiles})
set allfiles=(${compilefiles})
#
javac ${coptions} ${compilefiles}
#
set compilefiles=iiv/dialog/CNUDialog.java
set compilefiles=(${compilefiles} iiv/dialog/ChoiceDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/ContinueDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/CropDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/QueryDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/StatusWindow.java)
set compilefiles=(${compilefiles} iiv/dialog/StatusWindowShowPointDisplay.java)
set jarfiles=(${jarfiles} ${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
#
set compilefiles=iiv/display/CNUColorModel.java
set compilefiles=(${compilefiles} iiv/display/CNUContainer.java)
set compilefiles=(${compilefiles} iiv/display/CNUDisplay.java)
set compilefiles=(${compilefiles} iiv/display/CNUDisplayConstraints.java)
set compilefiles=(${compilefiles} iiv/display/ColorMapException.java)
set compilefiles=(${compilefiles} iiv/display/ComponentGroup.java)
set compilefiles=(${compilefiles} iiv/display/DisplayColorMap.java)
set compilefiles=(${compilefiles} iiv/display/DisplayColorMapQuilt.java)
set compilefiles=(${compilefiles} iiv/display/DisplayComponent.java)
set compilefiles=(${compilefiles} iiv/display/DisplayComponentDefaults.java)
set compilefiles=(${compilefiles} iiv/display/DisplayDraw.java)
set compilefiles=(${compilefiles} iiv/display/DisplayShape.java)
set compilefiles=(${compilefiles} iiv/display/DisplayText.java)
set compilefiles=(${compilefiles} iiv/display/GreyColorModel.java)
set compilefiles=(${compilefiles} iiv/display/iiVTransferable.java)
set compilefiles=(${compilefiles} iiv/display/IntensityProjectionImage.java)
set compilefiles=(${compilefiles} iiv/display/LocationMapping.java)
set compilefiles=(${compilefiles} iiv/display/NumberFormattable.java)
set compilefiles=(${compilefiles} iiv/display/Overlayable.java)
set compilefiles=(${compilefiles} iiv/display/ScriptableDisplayComponent.java)
set compilefiles=(${compilefiles} iiv/display/ShowPointController.java)
set compilefiles=(${compilefiles} iiv/display/ShowPointControllerInterface.java)
set compilefiles=(${compilefiles} iiv/display/ShowPointDialogInterface.java)
set compilefiles=(${compilefiles} iiv/display/ShowPointDisplay.java)
set compilefiles=(${compilefiles} iiv/display/ShowPointDisplayLine.java)
set compilefiles=(${compilefiles} iiv/display/ShowPointImage.java)
set compilefiles=(${compilefiles} iiv/display/ShowPointTracker.java)
set compilefiles=(${compilefiles} iiv/display/SimplePolygon.java)
set compilefiles=(${compilefiles} iiv/display/SingleImg.java)
set compilefiles=(${compilefiles} iiv/display/SliceNumbering.java)
# used by colordialogs
set compilefiles=(${compilefiles} iiv/display/SingleComponentCanvas.java)
set compilefiles=(${compilefiles} iiv/display/ColorMapCanvas.java)
set compilefiles=(${compilefiles} iiv/display/ColorMapQuiltCanvas.java)
# not refrenced by iiV but may be displayed
set compilefiles=(${compilefiles} iiv/display/DisplayBox.java)
set compilefiles=(${compilefiles} iiv/display/DisplayLine.java)
set compilefiles=(${compilefiles} iiv/display/DisplayOval.java)
set jarfiles=(${jarfiles} ${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
#
set compilefiles=iiv/filter/ColorFilter.java
set compilefiles=(${compilefiles} iiv/filter/Croppable.java)
set compilefiles=(${compilefiles} iiv/filter/FilterSampling.java)
set compilefiles=(${compilefiles} iiv/filter/FlipFilter.java)
set compilefiles=(${compilefiles} iiv/filter/Flippable.java)
set compilefiles=(${compilefiles} iiv/filter/LinearImageFilter.java)
set compilefiles=(${compilefiles} iiv/filter/Mapping2D.java)
set compilefiles=(${compilefiles} iiv/filter/Rotatable.java)
set compilefiles=(${compilefiles} iiv/filter/ShapeColorFilter.java)
set compilefiles=(${compilefiles} iiv/filter/Zoomable.java)
#
set compilefiles=(${compilefiles} iiv/gui/CNURowLayoutManager.java)
set compilefiles=(${compilefiles} iiv/gui/EasyAddAbstractAction.java)
set compilefiles=(${compilefiles} iiv/gui/JTextComponentChangeListener.java)
set compilefiles=(${compilefiles} iiv/gui/TextAndSlider.java)
set compilefiles=(${compilefiles} iiv/gui/VisibleStateButtonUpdate.java)
set compilefiles=(${compilefiles} iiv/gui/AutoExtendBoundedRangeModel.java)
set jarfiles=(${jarfiles} ${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
#
set compilefiles=iiv/io/AnalyzeColorMap.java
set compilefiles=(${compilefiles} iiv/io/CNUDataConversions.java)
set compilefiles=(${compilefiles} iiv/io/CNUFile.java)
set compilefiles=(${compilefiles} iiv/io/CNUFileObject.java)
set compilefiles=(${compilefiles} iiv/io/CNUImgFile.java)
set compilefiles=(${compilefiles} iiv/io/CNUIntImage.java)
set compilefiles=(${compilefiles} iiv/io/CNURawImgFile.java)
set compilefiles=(${compilefiles} iiv/io/CNUStdImgFile.java)
set compilefiles=(${compilefiles} iiv/io/ConvertDataInputStream.java)
set compilefiles=(${compilefiles} iiv/io/SleepBufferedReader.java)
set compilefiles=(${compilefiles} iiv/io/SleepFileReader.java)
set compilefiles=(${compilefiles} iiv/io/SleepInputStreamReader.java)
set compilefiles=(${compilefiles} iiv/io/SleepReader.java)
set jarfiles=(${jarfiles} ${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
#

set compilefiles=iiv/script/CNUDisplayScript.java
set compilefiles=(${compilefiles} iiv/script/CNUScriptObjects.java)
set compilefiles=(${compilefiles} iiv/script/CNUScriptVariables.java)
set compilefiles=(${compilefiles} iiv/script/iiVBshScript.java)
set compilefiles=(${compilefiles} iiv/script/iiVBshJConsole.java)
set compilefiles=(${compilefiles} iiv/script/iiVScriptable.java)
set compilefiles=(${compilefiles} iiv/script/LineParseException.java)
set compilefiles=(${compilefiles} iiv/script/ObjectConstructionException.java)
#
set compilefiles=(${compilefiles} iiv/util/BrowserControl.java)
set compilefiles=(${compilefiles} iiv/util/DisplayNumberFormat.java)
set compilefiles=(${compilefiles} iiv/util/DoCommand.java)
set compilefiles=(${compilefiles} iiv/util/FileListElement.java)
set compilefiles=(${compilefiles} iiv/util/FormatTools.java)
set compilefiles=(${compilefiles} iiv/util/RunnableWithReturnObject.java)
set compilefiles=(${compilefiles} iiv/util/ShowStatus.java)
set compilefiles=(${compilefiles} iiv/util/UndoButtons.java)
set compilefiles=(${compilefiles} iiv/util/UndoRedo.java)
set jarfiles=(${jarfiles} ${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
#
# create part jar class file
set jarpartclassfile=${jardir}/iiV${version}main.jar
${jarcmd} cvfm ${jarpartclassfile} multiJarMainClass LICENSE changes
(cd ${classdir}; ${jarcmd} uvf ${jarpartclassfile} ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/})
# add to all in one jar files
(cd ${classdir}; ${jarcmd} uvf ${jarclassfile} ${jarfiles:gs/.java/.class/} ${allfiles:gs/.java/$*.class/})
${jarcmd} uvf ${jarsrcfile} ${jarfiles}
#
set compilefiles=iiv/dialog/ColorDialog.java
set compilefiles=(${compilefiles} iiv/dialog/CoordinateMapDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/DataSlicerDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/EditColorDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/FileTypeDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/FilterDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/FormatDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/GridDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/RegionDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/ScaleDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/ShapeDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/ShowMemoryDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/ShowPointDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/GotoPointDialog.java)
set compilefiles=(${compilefiles} iiv/dialog/TextDialog.java)
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}

# create part jar class file
set jarpartclassfile=${jardir}/iiV${version}dialogs.jar
${jarcmd} cvfm ${jarpartclassfile} multiJarMainClass LICENSE changes
(cd ${classdir}; ${jarcmd} uvf ${jarpartclassfile} ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/})
# add to all in one jar files
(cd ${classdir}; ${jarcmd} uvf ${jarclassfile} ${jarfiles:gs/.java/.class/} ${allfiles:gs/.java/$*.class/})
${jarcmd} uvf ${jarsrcfile} ${jarfiles}

#

set compilefiles=iiv/CNUViewerMenuBar.java
set compilefiles=(${compilefiles} iiv/gui/MenuList.java)
set compilefiles=(${compilefiles} iiv/display/DisplayImagePopupMenu.java)
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}

# create part jar class file
set jarpartclassfile=${jardir}/iiV${version}mainmenu.jar
${jarcmd} cvfm ${jarpartclassfile} multiJarMainClass LICENSE changes
(cd ${classdir}; ${jarcmd} uvf ${jarpartclassfile} ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/})
# add to all in one jar files
(cd ${classdir}; ${jarcmd} uvf ${jarclassfile} ${jarfiles:gs/.java/.class/} ${allfiles:gs/.java/$*.class/})
${jarcmd} uvf ${jarsrcfile} ${jarfiles}

#
set compilefiles=iiv/dialog/ControlDialog.java
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}

set jarpartclassfile=${jardir}/iiV${version}mainpanel.jar
${jarcmd} cvfm ${jarpartclassfile} multiJarMainClass LICENSE changes
(cd ${classdir}; ${jarcmd} uvf ${jarpartclassfile} ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/})
# add to all in one jar files
(cd ${classdir}; ${jarcmd} uvf ${jarclassfile} ${jarfiles:gs/.java/.class/} ${allfiles:gs/.java/$*.class/})
${jarcmd} uvf ${jarsrcfile} ${jarfiles}

#
set compilefiles=iiv/dialog/SaveDialog.java
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}

set jarpartclassfile=${jardir}/iiV${version}save.jar
${jarcmd} cvfm ${jarpartclassfile} multiJarMainClass LICENSE changes
(cd ${classdir}; ${jarcmd} uvf ${jarpartclassfile} ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/})
# add to all in one jar files
(cd ${classdir}; ${jarcmd} uvf ${jarclassfile} ${jarfiles:gs/.java/.class/} ${allfiles:gs/.java/$*.class/})
${jarcmd} uvf ${jarsrcfile} ${jarfiles}

#
set compilefiles=iiv/io/AnalyzeHeader.java
set compilefiles=(${compilefiles} iiv/io/CMRRSdtImgFile.java)
set compilefiles=(${compilefiles} iiv/io/CNUAnalyzeImgFile.java)
set compilefiles=(${compilefiles} iiv/io/CNUEcat7Header.java)
set compilefiles=(${compilefiles} iiv/io/CNUEcat7ImgFile.java)
set compilefiles=(${compilefiles} iiv/io/CNUEcatHeader.java)
set compilefiles=(${compilefiles} iiv/io/CNUEcatImgFile.java)
set compilefiles=(${compilefiles} iiv/io/CNUNiftiImgFile.java)
set compilefiles=(${compilefiles} iiv/io/DICOM_DataElement.java)
set compilefiles=(${compilefiles} iiv/io/DICOMImgFile.java)
set compilefiles=(${compilefiles} iiv/io/DICOM_Tag.java)
set compilefiles=(${compilefiles} iiv/io/DICOM_VR.java)
set compilefiles=(${compilefiles} iiv/io/NiftiHeader.java)
set compilefiles=(${compilefiles} iiv/io/ThreeDSSPFile.java)
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
set jarpartclassfile=${jardir}/iiV${version}fileformats.jar
${jarcmd} cvfm ${jarpartclassfile} multiJarMainClass LICENSE changes
(cd ${classdir}; ${jarcmd} uvf ${jarpartclassfile} ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/})
# add to all in one jar files
(cd ${classdir}; ${jarcmd} uvf ${jarclassfile} ${jarfiles:gs/.java/.class/} ${allfiles:gs/.java/$*.class/})
${jarcmd} uvf ${jarsrcfile} ${jarfiles}

#
# add bean shell
${jarcmd} uvf ${jarclassfile} bsh
${jarcmd} uvf ${jarsrcfile} bsh
# finishing with Acme files
${jarcmd} uvf ${jarclassfile} Acme
${jarcmd} uvf ${jarsrcfile} Acme
#
# build API html documentations for all classes
javadoc ${docoptions}  -bottom '<i>Copyright &copy; 2018 Cognitive Neuroimaging Unit, VA Medical Center, Minneapolis, MN</i>' ${allfiles}
#
#end csh
