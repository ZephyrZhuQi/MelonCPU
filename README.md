MelonCPU
=============

a CPU written in Chisel(Constructing Hardware in a Scala Embedded Language)

a Pipeline CPU based on MIPS Instruction Set
-------------

Initially it is the course requirement completed by a team of four.

Running this CPU
-------------

To get started:

	$ sbt

Test single instruction file:
	
	> test:runMain PipeLine.TopTest

Test all instruction files:

	> test:runMain PipeLine.PipelineTopTests 

