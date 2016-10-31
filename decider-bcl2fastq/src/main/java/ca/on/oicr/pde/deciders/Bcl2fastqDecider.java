package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.model.LaneProvenance;
import ca.on.oicr.gsi.provenance.model.LimsKey;
import ca.on.oicr.gsi.provenance.model.LimsProvenance;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pde.deciders.configuration.StudyToOutputPathConfig;
import ca.on.oicr.pde.deciders.handlers.Bcl2Fastq1Handler;
import ca.on.oicr.pde.deciders.handlers.Bcl2Fastq2Handler;
import ca.on.oicr.pde.deciders.handlers.Bcl2FastqData;
import ca.on.oicr.pde.deciders.handlers.Bcl2FastqHandler;
import ca.on.oicr.pde.deciders.handlers.Handler;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.pipeline.runner.PluginRunner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author mlaszloffy
 */
public class Bcl2fastqDecider {

    private final Logger log = LogManager.getLogger(Bcl2fastqDecider.class);
    private final List<Bcl2FastqHandler> handlers = new ArrayList<>();

    private ExtendedProvenanceClient provenanceClient;
    private Metadata metadata;
    private Map<String, String> config;
    private Workflow workflow;
    private Integer launchMax = 10;
    private Set<String> workflowAccessionsToCheck = Collections.EMPTY_SET;
    private String host;

    private Boolean isDryRunMode = false;
    private Boolean doMetadataWriteback = true;
    private Boolean ignorePreviousAnalysisMode = false;
    private Boolean ignorePreviousLimsKeysMode = false;
    private Boolean disableRunCompleteCheck = false;

    private String outputPath = "./";
    private String outputFolder = "seqware-results";
    private StudyToOutputPathConfig studyToOutputPathConfig;

    private Boolean replaceNullCreatedDate = false;
    private DateTime afterDateFilter = null;
    private DateTime beforeDateFilter = null;
    private Set<String> includeInstrumentNameFilter;
    private Set<String> excludeInstrumentNameFilter;
    private EnumMap<FileProvenanceFilter, Set<String>> includeFilters = new EnumMap<>(FileProvenanceFilter.class);
    private EnumMap<FileProvenanceFilter, Set<String>> excludeFilters = new EnumMap<>(FileProvenanceFilter.class);

    private List<String> overrides;

    public Bcl2fastqDecider() {
        //add workflow handlers
        handlers.add(new Bcl2Fastq1Handler()); //CASAVA 2.7 handler
        handlers.add(new Bcl2Fastq2Handler()); //CASAVA 2.8+ handler
    }

    public static Set<FileProvenanceFilter> getSupportedFilters() {
        return ImmutableSet.of(
                FileProvenanceFilter.lane,
                FileProvenanceFilter.sequencer_run,
                FileProvenanceFilter.study,
                FileProvenanceFilter.sequencer_run_platform_model
        );
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getIsDryRunMode() {
        return isDryRunMode;
    }

    public void setIsDryRunMode(Boolean isDryRunMode) {
        this.isDryRunMode = isDryRunMode;
    }

    public Boolean getDoMetadataWriteback() {
        return doMetadataWriteback;
    }

    public void setDoMetadataWriteback(Boolean doMetadataWriteback) {
        this.doMetadataWriteback = doMetadataWriteback;
    }

    public Set<String> getWorkflowAccessionsToCheck() {
        return workflowAccessionsToCheck;
    }

    public void setWorkflowAccessionsToCheck(Set<String> workflowAccessionsToCheck) {
        this.workflowAccessionsToCheck = workflowAccessionsToCheck;
    }

    public Integer getLaunchMax() {
        return launchMax;
    }

    public void setLaunchMax(Integer launchMax) {
        this.launchMax = launchMax;
    }

    public boolean isIgnorePreviousAnalysisMode() {
        return ignorePreviousAnalysisMode;
    }

    public void setIgnorePreviousAnalysisMode(boolean ignorePreviousAnalysisMode) {
        this.ignorePreviousAnalysisMode = ignorePreviousAnalysisMode;
    }

    public boolean isIgnorePreviousLimsKeysMode() {
        return ignorePreviousLimsKeysMode;
    }

    public void setIgnorePreviousLimsKeysMode(boolean ignorePreviousLimsKeysMode) {
        this.ignorePreviousLimsKeysMode = ignorePreviousLimsKeysMode;
    }

    public boolean isDisableRunCompleteCheck() {
        return disableRunCompleteCheck;
    }

    public void setDisableRunCompleteCheck(boolean disableRunCompleteCheck) {
        this.disableRunCompleteCheck = disableRunCompleteCheck;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public StudyToOutputPathConfig getStudyToOutputPathConfig() {
        return studyToOutputPathConfig;
    }

    public void setStudyToOutputPathConfig(StudyToOutputPathConfig studyToOutputPathConfig) {
        this.studyToOutputPathConfig = studyToOutputPathConfig;
    }

    public boolean isReplaceNullCreatedDate() {
        return replaceNullCreatedDate;
    }

    public void setReplaceNullCreatedDate(boolean replaceNullCreatedDate) {
        this.replaceNullCreatedDate = replaceNullCreatedDate;
    }

    public DateTime getAfterDateFilter() {
        return afterDateFilter;
    }

    public void setAfterDateFilter(DateTime afterDateFilter) {
        this.afterDateFilter = afterDateFilter;
    }

    public DateTime getBeforeDateFilter() {
        return beforeDateFilter;
    }

    public void setBeforeDateFilter(DateTime beforeDateFilter) {
        this.beforeDateFilter = beforeDateFilter;
    }

    public Set<String> getIncludeInstrumentNameFilter() {
        return includeInstrumentNameFilter;
    }

    public void setIncludeInstrumentNameFilter(Set<String> includeInstrumentNameFilter) {
        this.includeInstrumentNameFilter = includeInstrumentNameFilter;
    }

    public Set<String> getExcludeInstrumentNameFilter() {
        return excludeInstrumentNameFilter;
    }

    public void setExcludeInstrumentNameFilter(Set<String> excludeInstrumentNameFilter) {
        this.excludeInstrumentNameFilter = excludeInstrumentNameFilter;
    }

    public List<String> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<String> overrides) {
        this.overrides = overrides;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public EnumMap<FileProvenanceFilter, Set<String>> getIncludeFilters() {
        return includeFilters;
    }

    public void setIncludeFilters(EnumMap<FileProvenanceFilter, Set<String>> includeFilters) {
        this.includeFilters = includeFilters;
    }

    public EnumMap<FileProvenanceFilter, Set<String>> getExcludeFilters() {
        return excludeFilters;
    }

    public void setExcludeFilters(EnumMap<FileProvenanceFilter, Set<String>> excludeFilters) {
        this.excludeFilters = excludeFilters;
    }

    public void setProvenanceClient(ExtendedProvenanceClient provenanceClient) {
        this.provenanceClient = provenanceClient;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public List<WorkflowRun> run() {
        checkNotNull(workflow);

        //get workflow handler
        String workflowName = workflow.getName();
        String workflowVersion = workflow.getVersion();
        if (!hasHandler(workflowName, workflowVersion)) {
            throw new RuntimeException("Workflow [" + workflowName + "-" + workflowVersion + "] is not supported");
        }
        Bcl2FastqHandler handler = getHandler(workflowName, workflowVersion);

        // provenance data structures
        ListMultimap<String, ProvenanceWithProvider<LaneProvenance>> laneNameToLaneProvenance = ArrayListMultimap.create();
        Map<String, String> providerAndIdToLaneName = new HashMap<>();

        // get all lane provenance from providers specified in provenance settings
        Map<String, Collection<LaneProvenance>> laneProvenanceByProvider = provenanceClient.getLaneProvenanceByProvider(Collections.EMPTY_MAP); //filters);
        for (Map.Entry<String, Collection<LaneProvenance>> e : laneProvenanceByProvider.entrySet()) {
            String provider = e.getKey();
            for (LaneProvenance lp : e.getValue()) {
                String laneName = lp.getSequencerRunName() + "_lane_" + lp.getLaneNumber();
                providerAndIdToLaneName.put(provider + lp.getProvenanceId(), laneName);

                if (lp.getSkip()) {
                    log.debug("Lane = [{}] is skipped", laneName);
                    continue;
                }

                DateTime createdDate;
                if (replaceNullCreatedDate && lp.getCreatedDate() == null) { //ignore created date it is null
                    createdDate = lp.getLastModified();
                } else {
                    createdDate = lp.getCreatedDate();
                }

                if (createdDate == null) {
                    log.warn("Lane = [{}] has a null created date - treating lane as incomplete", laneName);
                    continue;
                }

                if (afterDateFilter != null && createdDate.isBefore(afterDateFilter)) {
                    continue;
                }

                if (beforeDateFilter != null && createdDate.isAfter(beforeDateFilter)) {
                    continue;
                }

                if (includeFilters.containsKey(FileProvenanceFilter.sequencer_run)
                        && !includeFilters.get(FileProvenanceFilter.sequencer_run).contains(lp.getSequencerRunName())) {
                    continue;
                }

                if (includeFilters.containsKey(FileProvenanceFilter.lane)
                        && !includeFilters.get(FileProvenanceFilter.lane).contains(laneName)) {
                    continue;
                }

                if (includeFilters.containsKey(FileProvenanceFilter.sequencer_run_platform_model)
                        && !includeFilters.get(FileProvenanceFilter.sequencer_run_platform_model).contains(lp.getSequencerRunPlatformModel())) {
                    continue;
                }

                if (includeInstrumentNameFilter != null
                        && !CollectionUtils.containsAny(includeInstrumentNameFilter, lp.getSequencerRunAttributes().get("instrument_name"))) {
                    continue;
                }

                if (excludeFilters.containsKey(FileProvenanceFilter.sequencer_run)
                        && excludeFilters.get(FileProvenanceFilter.sequencer_run).contains(lp.getSequencerRunName())) {
                    continue;
                }

                if (excludeFilters.containsKey(FileProvenanceFilter.lane)
                        && excludeFilters.get(FileProvenanceFilter.lane).contains(laneName)) {
                    continue;
                }

                if (excludeFilters.containsKey(FileProvenanceFilter.sequencer_run_platform_model)
                        && excludeFilters.get(FileProvenanceFilter.sequencer_run_platform_model).contains(lp.getSequencerRunPlatformModel())) {
                    continue;
                }

                if (excludeInstrumentNameFilter != null
                        && CollectionUtils.containsAny(excludeInstrumentNameFilter, lp.getSequencerRunAttributes().get("instrument_name"))) {
                    continue;
                }

                laneNameToLaneProvenance.put(laneName, new ProvenanceWithProvider<>(provider, lp));
            }
        }

        // get all sample provenance from providers specified in provenance settings
        ListMultimap<String, ProvenanceWithProvider<SampleProvenance>> laneNameToSampleProvenance = ArrayListMultimap.create();
        SetMultimap<String, String> laneNameToStudyNames = HashMultimap.create();
        Map<String, Collection<SampleProvenance>> sampleProvenanceByProvider = provenanceClient.getSampleProvenanceByProvider(Collections.EMPTY_MAP);
        for (Map.Entry<String, Collection<SampleProvenance>> e : sampleProvenanceByProvider.entrySet()) {
            String provider = e.getKey();
            for (SampleProvenance sp : e.getValue()) {
                String laneName = sp.getSequencerRunName() + "_lane_" + sp.getLaneNumber();
                providerAndIdToLaneName.put(provider + sp.getProvenanceId(), laneName);

                if (sp.getSkip()) {
                    log.debug("Sample = [{}] in lane = [{}] is skipped", sp.getSampleName(), laneName);
                    continue;
                }

                laneNameToStudyNames.put(laneName, sp.getStudyTitle());
                laneNameToSampleProvenance.put(laneName, new ProvenanceWithProvider<>(provider, sp));
            }
        }

        if (includeFilters.containsKey(FileProvenanceFilter.study)) {
            Set<String> lanesToRemove = new HashSet<>();
            Set<String> includeStudies = includeFilters.get(FileProvenanceFilter.study);

            for (Entry<String, Set<String>> e : Multimaps.asMap(laneNameToStudyNames).entrySet()) {
                String laneName = e.getKey();
                Set<String> laneStudies = e.getValue();

                //the set of studies in the lane that are not in the include studies list
                Set<String> additionalStudiesInLane = Sets.difference(laneStudies, includeStudies);

                if (additionalStudiesInLane.isEmpty()) {
                    //lane passed study filter
                } else {
                    log.debug("Lane = [{}] with studies = [{}] removed due to study filter", laneName, Joiner.on(",").join(laneStudies));
                    lanesToRemove.add(e.getKey());
                }
            }
            laneNameToLaneProvenance.keySet().removeAll(lanesToRemove);
        }

        if (excludeFilters.containsKey(FileProvenanceFilter.study)) {
            Set<String> lanesToRemove = new HashSet<>();
            Set<String> excludeStudies = excludeFilters.get(FileProvenanceFilter.study);

            for (Entry<String, Set<String>> e : Multimaps.asMap(laneNameToStudyNames).entrySet()) {
                String laneName = e.getKey();
                Set<String> laneStudies = e.getValue();

                //the set of studies in the lane that are in the exclude studies list
                Set<String> matches = Sets.intersection(laneStudies, excludeStudies);

                if (matches.isEmpty()) {
                    //lane passed study filter
                } else {
                    log.debug("Lane = [{}] with studies = [{}] removed due to study filter", laneName, Joiner.on(",").join(laneStudies));
                    lanesToRemove.add(e.getKey());
                }
            }
            laneNameToLaneProvenance.keySet().removeAll(lanesToRemove);
        }

        //get previous analysis
        Map<FileProvenanceFilter, Set<String>> analysisFilters = new HashMap<>();
        //TODO: seqware currently does not support retrieving FP with null workflow swids
        //Set<String> workflowSwidsToCheck = new HashSet<>();
        //workflowSwidsToCheck.add(getWorkflowAccession());
        //workflowSwidsToCheck.add(null);
        //workflowSwidsToCheck.addAll(getWorkflowAccessionsToCheck());
        //analysisFilters.put(FileProvenanceFilter.workflow, workflowSwidsToCheck);

        //the set of all lanes that have been analyzed using the current workflow or a workflow in the set of "check workflows"
        Set<String> analyzedLanes = new HashSet<>();

        //calculate the set of lanes that do not have associated workflow information but are linked in seqware
        Set<String> blockedLanes = new HashSet<>();

        Collection<FileProvenance> fps = provenanceClient.getFileProvenance(analysisFilters);
        for (FileProvenance fp : fps) {
            if (fp.getWorkflowSWID() != null) { //analysis provenance for a worklow run
                if (getWorkflowAccessionsToCheck().contains(fp.getWorkflowSWID().toString())
                        || workflow.getSwAccession().equals(fp.getWorkflowSWID())) {
                    analyzedLanes.addAll(fp.getLaneNames());
                }
            } else { //analysis provenance for an ius
                //include this record, it may be an IUS that is used for skipping a lane or sample
                blockedLanes.addAll(fp.getLaneNames());
            }
        }

        //calculate the set of all lanes that are known by the set of lane provenance providers
        Set<String> knownLanes = laneNameToLaneProvenance.keySet();

        //calculate the set of all lanes that are known and valid
        Set<String> allowedLanes = Sets.difference(knownLanes, blockedLanes);

        //calculate the set of lanes that have not been processed
        Set<String> unprocessedLanes = Sets.difference(allowedLanes, analyzedLanes);

        //filter candidate lanes into lanes to analyze
        Set<String> candidateLanesToAnalyze = new HashSet<>();
        if (ignorePreviousLimsKeysMode) {
            candidateLanesToAnalyze.addAll(knownLanes);
        } else if (ignorePreviousAnalysisMode) {
            candidateLanesToAnalyze.addAll(allowedLanes);
        } else {
            candidateLanesToAnalyze.addAll(unprocessedLanes);
        }

        //validation before scheduling workflow run
        Set<String> invalidLanes = new HashSet<>();
        for (String laneName : candidateLanesToAnalyze) {

            //expect one and only one lane provenance per lane name
            List<ProvenanceWithProvider<LaneProvenance>> lps = laneNameToLaneProvenance.get(laneName);
            if (lps.size() != 1) {
                invalidLanes.add(laneName);
                log.warn("Lane = [{}] can not be processed, lane provenance count = [{}], expected 1", laneName, lps.size());
            }

            //expect one or more sample provenance per lane name
            List<ProvenanceWithProvider<SampleProvenance>> sps = laneNameToSampleProvenance.get(laneName);
            if (sps.isEmpty()) {
                invalidLanes.add(laneName);
                log.warn("Lane = [{}] can not be processed, sample provenance count = [{}], expected 1 or more", laneName, sps.size());
            }

            if (!disableRunCompleteCheck) {
                if (lps.size() == 1) {
                    Set<String> runDirs = Iterables.getOnlyElement(lps).getProvenance().getSequencerRunAttributes().get("run_dir");
                    if (runDirs != null && runDirs.size() == 1) {
                        Path runDirPath = Paths.get(Iterables.getOnlyElement(Iterables.getOnlyElement(lps).getProvenance().getSequencerRunAttributes().get("run_dir")));
                        File runDir = runDirPath.toFile();
                        if (runDir.exists() && runDir.isDirectory() && runDir.canRead()) {
                            File oicrRunCompleteTouchFile = runDirPath.resolve("oicr_run_complete").toFile();
                            if (oicrRunCompleteTouchFile.exists()) {
                                //run is complete
                            } else {
                                log.info("Lane = [{}] has not completed sequencing ([{}] is missing)", laneName, oicrRunCompleteTouchFile.getAbsolutePath());
                                invalidLanes.add(laneName);
                            }
                        } else {
                            log.info("Lane = [{}] run_dir = [{}] is not accessible or does not exist", laneName, runDir.getAbsolutePath());
                            invalidLanes.add(laneName);
                        }
                    } else {
                        log.warn("Lane = [{}] can not be processed, run_dir = [{}]", laneName, (runDirs == null ? "" : Joiner.on(",").join(runDirs)));
                        invalidLanes.add(laneName);
                    }
                }
            }

        }

        //remove invalid lanes from lanes to analyze set
        Set<String> lanesToAnalyze = Sets.difference(candidateLanesToAnalyze, invalidLanes);

        //collect required workflow run data - create IUS-LimsKey records in seqware that will be linked to the workflow run
        List<WorkflowRun> workflowRuns = new ArrayList<>();
        int workflowRunValidationErrors = 0;
        for (String laneName : lanesToAnalyze) {

            if (launchMax != null && workflowRuns.size() >= launchMax) {
                log.info("Launch max reached");
                break;
            }

            try {
                ProvenanceWithProvider<LaneProvenance> lane = Iterables.getOnlyElement(laneNameToLaneProvenance.get(laneName));
                IusWithProvenance<ProvenanceWithProvider<LaneProvenance>> linkedLane = createIusToProvenanceLink(lane, getDoMetadataWriteback());
                LaneProvenance lp = lane.getProvenance();

                List<ProvenanceWithProvider<SampleProvenance>> samples = laneNameToSampleProvenance.get(laneName);
                List<IusWithProvenance<ProvenanceWithProvider<SampleProvenance>>> linkedSamples = new ArrayList<>();
                for (ProvenanceWithProvider<SampleProvenance> provenanceWithProvider : samples) {
                    linkedSamples.add(createIusToProvenanceLink(provenanceWithProvider, getDoMetadataWriteback()));
                }
                List<SampleProvenance> sps = new ArrayList<>();
                for (ProvenanceWithProvider<SampleProvenance> spWithProvider : samples) {
                    sps.add(spWithProvider.getProvenance());
                }

                List<Integer> iusSwidsToLinkWorkflowRunTo = new ArrayList<>();
                iusSwidsToLinkWorkflowRunTo.add(linkedLane.getIusSwid());
                for (IusWithProvenance<ProvenanceWithProvider<SampleProvenance>> linkedSample : linkedSamples) {
                    iusSwidsToLinkWorkflowRunTo.add(linkedSample.getIusSwid());
                }

                Bcl2FastqData data = new Bcl2FastqData();
                data.setIusSwidsToLinkWorkflowRunTo(iusSwidsToLinkWorkflowRunTo);
                data.setLinkedLane(linkedLane);
                data.setLinkedSamples(linkedSamples);
                data.setLp(lp);
                data.setSps(sps);
                data.setProperties(ImmutableMap.of("output_prefix", outputPath, "output_dir", outputFolder));
                data.setMetadataWriteback(getDoMetadataWriteback());
                data.setStudyToOutputPathConfig(studyToOutputPathConfig);

                WorkflowRun wr = handler.getWorkflowRun(data);
                workflowRuns.add(wr);
            } catch (DataMismatchException dme) {
                workflowRunValidationErrors++;
                log.error("Error while generating workflow run for lane = [{}]: ", laneName, dme);
            }
        }

        //schedule workflow runs
        List<WorkflowRun> scheduledWorkflowRuns = new ArrayList<>();
        for (WorkflowRun wr : workflowRuns) {
            //write ini properties to file
            Path iniFilePath = writeWorkflowRunIniPropertiesToFile(wr);

            WorkflowSchedulerCommandBuilder cmdBuilder = new WorkflowSchedulerCommandBuilder(workflow.getSwAccession());
            cmdBuilder.setIniFile(iniFilePath);
            cmdBuilder.setMetadataWriteback(getDoMetadataWriteback());
            cmdBuilder.setIusSwidsToLinkWorkflowRunTo(wr.getIusSwidsToLinkWorkflowRunTo());
            cmdBuilder.setOverrideArgs(overrides);
            List<String> runArgs = cmdBuilder.build();

            if (getIsDryRunMode()) {
                log.info("Dry-run mode: not scheduling workflow run\n"
                        + "Command: " + "java -jar seqware-distribution.jar " + Joiner.on(" ").join(runArgs) + "\n"
                        + "Ini:\n" + Joiner.on("\n").withKeyValueSeparator("=").join(wr.getIniFile()));
            } else {
                log.info("Scheduling workflow run");
                PluginRunner pluginRunner = new PluginRunner();
                pluginRunner.setConfig(config);
                pluginRunner.run(runArgs.toArray(new String[runArgs.size()]));
                scheduledWorkflowRuns.add(wr);
            }
        }

        log.info("Lane summary: fetched={} analyzed={} blocked={} allowed={} unprocessed={}",
                knownLanes.size(), analyzedLanes.size(), blockedLanes.size(), allowedLanes.size(), unprocessedLanes.size());
        log.info("Requested summary: requested={} invalid={} valid={}",
                candidateLanesToAnalyze.size(), invalidLanes.size(), lanesToAnalyze.size());
        log.info("Workflow run summary: candidate={} invalid={} valid={} scheduled={}",
                lanesToAnalyze.size(), workflowRunValidationErrors, workflowRuns.size(), scheduledWorkflowRuns.size());

        return scheduledWorkflowRuns;
    }

    private boolean hasHandler(String workflowName, String workflowVersion) {
        for (Handler h : handlers) {
            if (h.isHandlerFor(workflowName, workflowVersion)) {
                return true;
            }
        }
        return false;
    }

    private <T extends Handler> T getHandler(String workflowName, String workflowVersion) {
        T handler = null;
        for (Handler h : handlers) {
            if (h.isHandlerFor(workflowName, workflowVersion)) {
                if (handler == null) {
                    handler = (T) h;
                } else {
                    throw new RuntimeException("Multiple handlers for workflow = [" + workflowName + "-" + workflowVersion + "]");
                }
            }
        }
        if (handler == null) {
            throw new RuntimeException("No handlers for workflow = [" + workflowName + "-" + workflowVersion + "]");
        }
        return handler;
    }

    private <T extends LimsProvenance> IusWithProvenance<ProvenanceWithProvider<T>> createIusToProvenanceLink(ProvenanceWithProvider<T> p, boolean doMetadataWriteback) {
        Integer iusSwid;
        if (doMetadataWriteback) {
            LimsKey lk = p.getLimsKey();
            Integer limsKeySwid = metadata.addLimsKey(p.getProvider(), lk.getId(), lk.getVersion(), lk.getLastModified());
            iusSwid = metadata.addIUS(limsKeySwid, false);
        } else {
            iusSwid = 0;
        }

        return new IusWithProvenance<>(iusSwid, p);
    }

    private Path writeWorkflowRunIniPropertiesToFile(WorkflowRun wr) {
        try {
            Path iniFilePath = Files.createTempFile("bcl2fastq", ".ini");
            for (Map.Entry<String, String> e : wr.getIniFile().entrySet()) {
                String iniRecord = e.getKey() + "=" + e.getValue() + "\n";
                FileUtils.writeStringToFile(iniFilePath.toFile(), iniRecord, Charsets.UTF_8, true);
            }
            return iniFilePath;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
