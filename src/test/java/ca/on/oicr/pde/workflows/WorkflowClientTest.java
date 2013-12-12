package ca.on.oicr.pde.workflows;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class WorkflowClientTest {

    /**
     * Test of buildWorkflow method, of class WorkflowClient.
     */
    @Test
    public void parseLanesString() {
        
        String lanes = "6,631732:NoIndex,631733,PCSI_0046_Pa_P_PE_693_WG|"
                + "3,631709:NoIndex,631710,PCSI_0019_Pa_P_PE_562_WG|"
                + "7,631725:NoIndex,631726,PCSI_0072_Pa_P_PE_668_WG|"
                + "2,632022:NoIndex,632023,PCSI_0083_Du_R_PE_589_WG|"
                + "8,631703:NoIndex,631704,PCSI_0309_Ly_R_PE_750_WG|"
                + "1,632020:NoIndex,632021,PCSI_0083_Du_R_PE_593_WG|"
                + "4,631711:NoIndex,631712,PCSI_0047_Pa_P_PE_590_WG|"
                + "5,631723:NoIndex,631724,PCSI_0044_Si_R_PE_716_WG";
        List<ProcessEvent> ps = ProcessEvent.parseLanesString(lanes);
        Assert.assertEquals(ps.size(), 8);

    }

    @Test
    public void getLaneNumbers() {
        
        String input = "1,100:NoIndex,101,Sample_Name+AAAGTC,102,Sample_Name|"
                + "3,107:NoIndex,108,Sample_Name+AAAGTC,109,Sample_Name+NoIndex,110,Sample_Name|"
                + "3,107:NoIndex,111,Sample_Name+AAAGTC,112,Sample_Name+NoIndex,113,Sample_Name|"
                + "2,103:NoIndex,104,Sample_Name+AAAGTC,105,Sample_Name+NoIndex,106,Sample_Name";
        List<String> expected = Arrays.asList("1", "2", "3");
        List<String> actual = ProcessEvent.getUniqueSetOfLaneNumbers(ProcessEvent.parseLanesString(input));
        Assert.assertEquals(actual, expected);

    }

    @Test
    public void getLaneListFromLaneNumber() {
        
        String input = "1,100:NoIndex,101,Sample_Name+AAAGTC,102,Sample_Name|"
                + "2,103:NoIndex,104,Sample_Name+AAAGTC,105,Sample_Name+NoIndex,106,Sample_Name|"
                + "1,100:NoIndex,107,Sample_Name+AAAGTC,108,Sample_Name";
        Assert.assertTrue(ProcessEvent.getProcessEventListFromLaneNumber(ProcessEvent.parseLanesString(input), "1").size() == 4);
        Assert.assertTrue(ProcessEvent.getProcessEventListFromLaneNumber(ProcessEvent.parseLanesString(input), "2").size() == 3);
        Assert.assertTrue(ProcessEvent.getProcessEventListFromLaneNumber(ProcessEvent.parseLanesString(input), "3").isEmpty());
        
    }

    @Test
    public void getBarcodeStringFromLaneList() {
        
        String input = "1,100:NoIndex,101,Sample_Name+AAAGTC,102,Sample_Name|"
                + "2,103:NoIndex,104,Sample_Name+AAAGTC,105,Sample_Name+NoIndex,106,Sample_Name|"
                + "1,100:NoIndex,107,Sample_Name+AAAGTC,108,Sample_Name";
        String expected = "NoIndex,101,Sample_Name+AAAGTC,102,Sample_Name+NoIndex,107,Sample_Name+AAAGTC,108,Sample_Name";
        String actual = ProcessEvent.getBarcodesStringFromProcessEventList(ProcessEvent.getProcessEventListFromLaneNumber(ProcessEvent.parseLanesString(input), "1"));
        Assert.assertEquals(actual, expected);
        
    }
    
    @Test
    public void outputString() {
        
        String expected = "/tmp/Unaligned_110916_SN804_0064_AD04TBACXX_1/Project_na/"+
                "Sample_SWID_9858_PCSI_0106_Ly_R_PE_190_WG_110916_SN804_0064_AD04TBACXX/" + 
                "SWID_9858_PCSI_0106_Ly_R_PE_190_WG_110916_SN804_0064_AD04TBACXX_NoIndex_L001_R1_001.fastq.gz";
        String dataDir = "/tmp/";
        String flowcell = "110916_SN804_0064_AD04TBACXX";
        String laneNum = "1";
        String iusSwAccession = "9858";
        String sampleName = "PCSI_0106_Ly_R_PE_190_WG";
        String barcode = "NoIndex";
        String read  = "1";
        String actual = WorkflowClient.generateOutputPath(dataDir, flowcell, laneNum, iusSwAccession, sampleName, barcode, read);
        Assert.assertEquals(actual, expected);
        
    }
}