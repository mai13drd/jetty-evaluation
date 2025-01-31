# Jetty-Evaluation

This repository evaluates to which degree [Peass](https://github.com/DaGeRe/peass) is able to identify performance regressions in the application server jetty.

It is assumed that `$PROJECTFOLDER` is your local jetty folder (execute `git clone https://github.com/eclipse/jetty.project.git` to get the project) and that you have built the evaluation project (`mvn clean package`). Additionally, it is assumed that you can transfer the experimental repository using `$MYURL` (e.g. you could create a GitHub repository and and replace `$MYURL` by this repository).

## Tree Reading

- Instrument your project folder running `java -cp target/jetty-evaluation-0.1-SNAPSHOT.jar de.dagere.peassEvaluation.ExecutionPreparer $PROJECTFOLDER`
- Generate the jmh jar by `mvn -V -B clean install -DskipTests -Dlicense.skip -Denforcer.skip -Dcheckstyle.skip -T6 -e -pl :jetty-jmh -am`
- Execute the trace creation by `cp src/main/resources/getTrees.sh $PROJECTFOLDER/ && cd $PROJECTFOLDER && ./getTrees.sh`
- Analyse the traces running `java -cp target/jetty-evaluation-0.1-SNAPSHOT.jar de.dagere.peassEvaluation.GetTrees -projectFolder $PROJECTFOLDER -tree $PROJECTFOLDER/tree-results/traces/`

## Regression Injection

- Reset the project (e.g. using `cd $PROJECTFOLDER && git reset --hard`)
- Run `java -cp target/jetty-evaluation-0.1-SNAPSHOT.jar de.dagere.peassEvaluation.RegressionInjector -projectFolder $PROJECTFOLDER -dataFolder $PROJECTFOLDER/tree-results` to inject performance regression in your local repository. A file called regressions.csv will be created, which contains a list of the branch with the regression (e.g. `regression-0`) and the benchmark that is affected (e.g. `org.eclipse.jetty.util.thread.strategy.jmh.EWYKBenchmark`)
- Commit the repository and make it usable for measurement in your measurement slaves, usually by creating a new repository and calling `git remote add experimentrepo $MYURL`

## Measurement with JMH

- Clone your measurement version using `git clone $MYURL` to the local `$PROJECTFOLDER` on your measurement slave.
- Execute the measurement. This can either be done using start.sh if you got a slurm cluster available, or manually. If you want to execute the measurement manually, take each line and set `$regression` to the first column of the line file (e.g. `export regression=regression-0`) and `$benchmark` to the second column of the line (e.g. `export benchmark=org.eclipse.jetty.util.thread.strategy.jmh.EWYKBenchmark`), and then execute the measurements by calling `src/main/resources/runEvaluation.sh` (you will need to adapt the `$PATH` and the url that is cloned).


## Measurement with Peass
