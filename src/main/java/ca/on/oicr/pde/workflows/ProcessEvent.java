package ca.on.oicr.pde.workflows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author mlaszloffy
 */
public class ProcessEvent {
    private final String laneNumber;
    private final String laneSwAccession;
    private final String barcode;
    private final String iusSwAccession;
    private final String sampleName;

    public ProcessEvent(String laneNumber, String laneSwAccession, String barcode, String iusSwAccession, String sampleName) {
        this.laneNumber = laneNumber;
        this.laneSwAccession = laneSwAccession;
        this.barcode = barcode;
        this.iusSwAccession = iusSwAccession;
        this.sampleName = sampleName;
    }

    public String getLaneNumber() {
        return laneNumber;
    }

    public String getLaneSwAccession() {
        return laneSwAccession;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getIusSwAccession() {
        return iusSwAccession;
    }

    public String getSampleName() {
        return sampleName;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s, %s, %s]", laneNumber, laneSwAccession, barcode, iusSwAccession, sampleName);
    }

    public static List<ProcessEvent> parseLanesString(String lanes) {
        List<ProcessEvent> result = new ArrayList<ProcessEvent>();
        for (String lane : Arrays.asList(lanes.split("\\|"))) {
            String laneInfo = lane.substring(0, lane.indexOf(":"));
            String laneNumber = laneInfo.substring(0, laneInfo.indexOf(","));
            String laneSwAccession = laneInfo.substring(laneInfo.indexOf(",") + 1);
            for (String b : Arrays.asList(lane.substring(lane.indexOf(":") + 1).split("\\+"))) {
                String[] barcodeAttrs = b.split(",");
                String barcode = barcodeAttrs[0];
                String iusSwAccession = barcodeAttrs[1];
                String sampleName = barcodeAttrs[2];
                result.add(new ProcessEvent(laneNumber, laneSwAccession, barcode, iusSwAccession, sampleName));
            }
        }
        return result;
    }

    public static List<String> getUniqueSetOfLaneNumbers(List<ProcessEvent> ps) {
        Set<String> laneNumbers = new TreeSet<String>(); //treeset = sorted + distinct elements
        for (ProcessEvent p : ps) {
            laneNumbers.add(p.getLaneNumber());
        }
        return new ArrayList<String>(laneNumbers);
    }

    public static List<ProcessEvent> getProcessEventListFromLaneNumber(List<ProcessEvent> ps, String laneNumber) {
        List<ProcessEvent> result = new ArrayList<ProcessEvent>();
        for (ProcessEvent p : ps) {
            if (p.getLaneNumber().equals(laneNumber)) {
                result.add(p);
            }
        }
        return result;
    }

    public static String getBarcodesStringFromProcessEventList(List<ProcessEvent> ps) {
        StringBuilder sb = new StringBuilder();
        for (ProcessEvent p : ps) {
            sb.append(p.getBarcode()).append(",").append(p.getIusSwAccession()).append(",").append(p.getSampleName());
            sb.append("+");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
}