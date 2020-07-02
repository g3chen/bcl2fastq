workflow test_location {
	call find_tools
}

task find_tools {
	command {
		ls $BCL2FASTQ_JAIL_ROOT
                echo "@@@@@@@@@@@@@@@@"
                ls $BCL2FASTQ_ROOT
                echo "@@@@@@@@@@@@@@@@"
 
		echo $PATH
                echo "@@@@@@@@@@@@@@@@"
                echo $MANPATH
                echo "@@@@@@@@@@@@@@@@"
                echo $LD_LIBRARY_PATH
                echo "@@@@@@@@@@@@@@@@"
                echo $LD_RUN_PATH
	}
	output{
		String message = read_string(stdout())
	}
	runtime {
		docker: "g3chen/bcl2fastq@sha256:f097aef204d49795028e2e73ff256cba555b94ef9ab76caf57797a92fbe147c4"
                modules: "bcl2fastq/2.18.0.12 bcl2fastq-jail/3.0.0"
	}
}

