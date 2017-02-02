#!/bin/csh
set version=1187
# remember to update multiJarMainClass with the new version
#
set classpath=.:bsh.jar
set coptions="-classpath $classpath"
set docoptions="-public -classpath $classpath -d iiV${version}api -doctitle iiV${version} -header iiV${version} -footer iiV${version} -linkoffline http://java.sun.com/j2se/1.4/docs/api j2se1.4.2packages -linkoffline http://www.beanshell.org/javadoc bshpackages"
set jarcmd=jar
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
${jarcmd} cvfm iiV${version}main.jar multiJarMainClass LICENSE changes ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} cvfm iiV${version}.jar allInOneMainClass LICENSE changes ${jarfiles:gs/.java/.class/} ${allfiles:gs/.java/$*.class/}
${jarcmd} cvf iiV${version}src.jar allInOneMainClass multiJarMainClass LICENSE changes bldiiv.csh ${jarfiles}
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
${jarcmd} cvfm iiV${version}dialogs.jar multiJarMainClass LICENSE ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}.jar ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}src.jar ${jarfiles}
#

set compilefiles=iiv/CNUViewerMenuBar.java
set compilefiles=(${compilefiles} iiv/gui/MenuList.java)
set compilefiles=(${compilefiles} iiv/display/DisplayImagePopupMenu.java)
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
${jarcmd} cvfm iiV${version}mainmenu.jar multiJarMainClass LICENSE ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}.jar ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}src.jar ${jarfiles}
#
set compilefiles=iiv/dialog/ControlDialog.java
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
${jarcmd} cvfm iiV${version}mainpanel.jar multiJarMainClass LICENSE ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}.jar ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}src.jar ${jarfiles}
#
set compilefiles=iiv/dialog/SaveDialog.java
set jarfiles=(${compilefiles})
set allfiles=(${allfiles} ${compilefiles})
#
javac ${coptions} ${compilefiles}
${jarcmd} cvfm iiV${version}save.jar multiJarMainClass LICENSE ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/} Acme
${jarcmd} uf iiV${version}.jar ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}src.jar ${jarfiles}
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
javac ${coptions} ${compilefiles}
${jarcmd} cvfm iiV${version}fileformats.jar multiJarMainClass LICENSE ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}.jar ${jarfiles:gs/.java/.class/} ${jarfiles:gs/.java/$*.class/}
${jarcmd} uf iiV${version}src.jar ${jarfiles}
#
# add bean shell
${jarcmd} uf iiV${version}.jar bsh
${jarcmd} uf iiV${version}src.jar bsh
# finishing with Acme files
${jarcmd} uf iiV${version}.jar Acme
${jarcmd} uf iiV${version}src.jar Acme
#
# build API html documentations for all classes
javadoc ${docoptions}  -bottom '<i>Copyright &copy; 2007 Cognitive Neuroimaging Unit, VA Medical Center, Minneapolis, MN <br/>&lt;<a href="http://james.psych.umn.edu/">Cognitive Neuroimaging Unit Home Page</a>&gt;<br/><a href="mailto:webmaster@james.psych.umn.edu">webmaster@james.psych.umn.edu</a></i>' ${allfiles}
#
#end csh
