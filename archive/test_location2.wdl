workflow test_location {
    call find_tools
}

task find_tools {
    command <<<
        ls -l /data/test_bcl2fastq_tiny
        echo "@@@@@@@@@@@@@"
    >>>
    output{
        String message = read_string(stdout())
    }
    runtime {
        docker: "g3chen/bcl2fastq@sha256:f097aef204d49795028e2e73ff256cba555b94ef9ab76caf57797a92fbe147c4"
    }
}
