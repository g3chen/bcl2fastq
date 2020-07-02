workflow test_location {
	call find_tools
}

task find_tools {
	command {
		ls $BCL2FASTQ_JAIL_ROOT
                echo "@@@@@@@@@@@@@@@@"
                ls $BCL2FASTQ_ROOT
                echo "@@@@@@@@@@@@@@@@"
                ls $PYTHON_ROOT
                echo "@@@@@@@@@@@@@@@@"
                ls $BARCODEX_ROOT
                echo "@@@@@@@@@@@@@@@@"
 
		echo $PATH
                echo "@@@@@@@@@@@@@@@@"
                echo $MANPATH
                echo "@@@@@@@@@@@@@@@@"
                echo $LD_LIBRARY_PATH
                echo "@@@@@@@@@@@@@@@@"
                echo $LD_RUN_PATH
                echo "@@@@@@@@@@@@@@@@"
                echo $PKG_CONFIG_PATH
                echo "@@@@@@@@@@@@@@@@"
                echo $PYTHONPATH
                echo "@@@@@@@@@@@@@@@@"
	}
	output{
		String message = read_string(stdout())
	}
	runtime {
		docker: "g3chen/bcl2fastq@sha256:c7c2c719ef9e7809f162a33e6d830914c2b92d5abf3661661af409ae5bb85e83"
                modules: "bcl2fastq/2.18.0.12 bcl2fastq-jail/3.0.0 barcodex/1.0.5"
	}
}

