package de.dagere.peassEvaluation.regressionGeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peassEvaluation.GitRegressionBranchUtil;
import de.peass.dependency.analysis.data.ChangedEntity;
import de.peass.measurement.rca.data.CallTreeNode;
import de.peass.utils.Constants;
import de.peass.vcs.GitUtils;

public class RegressionBranchGenerator {

   private static final Logger LOG = LogManager.getLogger(RegressionBranchGenerator.class);

   private static final Random RANDOM = new Random();

   private CallTreeNode currentRoot;

   private final File projectFolder;

   public RegressionBranchGenerator(final File projectFolder) {
      this.projectFolder = projectFolder;
   }

   public void createRegressionBranch(final File[] trees, final FileWriter benchmarkNameWriter, final int regressionIndex)
         throws IOException, JsonParseException, JsonMappingException, InterruptedException, FileNotFoundException {
      CallTreeNode chosenLeaf = chooseLeaf(trees);

      GitUtils.goToTag("b56edf511ab4399122ea2c6162a4a5988870f479", projectFolder);
      GitRegressionBranchUtil.branch(projectFolder, "regression-" + regressionIndex);
      new CodeRegressionCreator(projectFolder).createCodeRegression(chosenLeaf.toEntity());
      GitRegressionBranchUtil.commit(projectFolder, "Create regression " + regressionIndex);

      String benchmarkName = currentRoot.getCall().substring("root-".length(), currentRoot.getCall().indexOf(ChangedEntity.METHOD_SEPARATOR));
      benchmarkNameWriter.write("regression-" + regressionIndex + ";" + benchmarkName + "\n");
      benchmarkNameWriter.flush();
   }

   private CallTreeNode chooseLeaf(final File[] trees) throws IOException, JsonParseException, JsonMappingException {
      File treeFile = trees[RANDOM.nextInt(trees.length)];

      currentRoot = Constants.OBJECTMAPPER.readValue(treeFile, CallTreeNode.class);

      List<CallTreeNode> leafs = getLeafs(currentRoot.getChildren());
      CallTreeNode chosenLeaf = leafs.get(RANDOM.nextInt(leafs.size()));
      if (chosenLeaf.getParent().getParent() == null) {
         LOG.debug("Only call trees with depth > 1 allowed");
         return chooseLeaf(trees);
      }
      int i = 0;
      while (!chosenLeaf.getCall().contains("<init>") && i < 10) {
         chosenLeaf = leafs.get(RANDOM.nextInt(leafs.size()));
         i++;
      }
      if (chosenLeaf.getCall().contains("<init>")) {
         return chooseLeaf(trees);
      } else {
         return chosenLeaf;
      }
   }

   private List<CallTreeNode> getLeafs(final List<CallTreeNode> level) {
      List<CallTreeNode> nextLevel = new LinkedList<>();
      for (CallTreeNode levelNode : level) {
         nextLevel.addAll(levelNode.getChildren());
      }
      if (nextLevel.isEmpty()) {
         return level;
      } else {
         return getLeafs(nextLevel);
      }
   }
}
