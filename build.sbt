name := "multir-framework"

version := "0.1"

fork := true

javaOptions in run += "-Xmx12G"

javaOptions in run += "-Djava.util.Arrays.useLegacyMergeSort=true"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-io" % "1.3.2",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-cli" % "commons-cli" % "1.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "1.3.5",
  "org.apache.derby" % "derby" % "10.10.1.1",
  "org.apache.derby" % "derbyclient" % "10.9.1.0")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
