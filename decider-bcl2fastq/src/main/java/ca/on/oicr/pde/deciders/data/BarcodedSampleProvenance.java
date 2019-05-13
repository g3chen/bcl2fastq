package ca.on.oicr.pde.deciders.data;

import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import java.time.ZonedDateTime;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Couple a SampleProvenance object with its Barcode or a custom Barcode.
 * <p>
 * BarcodedSampleProvenance can be used to describe a SampleProvenance object with a custom Barcode. "getIusTag()" will return the string
 * representation of the provided Barcode.
 * <p>
 * Note: The provided Barcode does not change the SampleProvenance "version".
 *
 * @author mlaszloffy
 */
public class BarcodedSampleProvenance implements SampleProvenance {

    private final SampleProvenance sp;
    private final Barcode barcode;

    public BarcodedSampleProvenance(SampleProvenance sp, Barcode barcode) {
        this.sp = sp;
        this.barcode = barcode;
    }

    public Barcode getBarcode() {
        return barcode;
    }

    @Override
    public String getStudyTitle() {
        return sp.getStudyTitle();
    }

    @Override
    public SortedMap<String, SortedSet<String>> getStudyAttributes() {
        return sp.getStudyAttributes();
    }

    @Override
    public String getRootSampleName() {
        return sp.getRootSampleName();
    }

    @Override
    public String getParentSampleName() {
        return sp.getParentSampleName();
    }

    @Override
    public String getSampleName() {
        return sp.getSampleName();
    }

    @Override
    public SortedMap<String, SortedSet<String>> getSampleAttributes() {
        return sp.getSampleAttributes();
    }

    @Override
    public String getSequencerRunName() {
        return sp.getSequencerRunName();
    }

    @Override
    public SortedMap<String, SortedSet<String>> getSequencerRunAttributes() {
        return sp.getSequencerRunAttributes();
    }

    @Override
    public String getSequencerRunPlatformModel() {
        return sp.getSequencerRunPlatformModel();
    }

    @Override
    public String getLaneNumber() {
        return sp.getLaneNumber();
    }

    @Override
    public SortedMap<String, SortedSet<String>> getLaneAttributes() {
        return sp.getLaneAttributes();
    }

    @Override
    public String getIusTag() {
        return barcode.toString();
    }

    @Override
    public Boolean getSkip() {
        return sp.getSkip();
    }

    @Override
    public String getSampleProvenanceId() {
        return sp.getSampleProvenanceId();
    }

    @Override
    public ZonedDateTime getCreatedDate() {
        return sp.getCreatedDate();
    }

    @Override
    public String getProvenanceId() {
        return sp.getProvenanceId();
    }

    @Override
    public String getVersion() {
        return sp.getVersion();
    }

    @Override
    public ZonedDateTime getLastModified() {
        return sp.getLastModified();
    }

}