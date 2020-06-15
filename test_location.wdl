workflow test_location {
	call find_tools
}

task find_tools {
	command {
		whereis rpm2cpio 
		whereis cpio
	}
	output{
		String message = read_string(stdout())
	}
	runtime {
		docker: "g3chen/bcl2fastq:1.0"
	}
}

