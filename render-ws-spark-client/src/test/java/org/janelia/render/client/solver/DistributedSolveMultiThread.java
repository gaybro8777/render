package org.janelia.render.client.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.janelia.render.client.ClientRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mpicbg.models.Affine2D;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.Model;
import mpicbg.models.NoninvertibleModelException;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.spim.io.IOFunctions;

public class DistributedSolveMultiThread< G extends Model< G > & Affine2D< G >, B extends Model< B > & Affine2D< B >, S extends Model< S > & Affine2D< S > > extends DistributedSolve< G, B, S >
{
	public DistributedSolveMultiThread(
			final G globalSolveModel,
			final B blockSolveModel,
			final S stitchingModel,
			final ParametersDistributedSolve parameters ) throws IOException

	{
		super( globalSolveModel, blockSolveModel, stitchingModel, parameters );
	}

	@Override
	public GlobalSolve solve()
	{
		final long time = System.currentTimeMillis();

		/*
		final DistributedSolveWorker< G, B, S > w = new DistributedSolveWorker<>(
				this.solveSet.leftItems.get( 0 ),
				runParams.pGroupList,
				runParams.sectionIdToZMap,
				parameters.renderWeb.baseDataUrl,
				parameters.renderWeb.owner,
				parameters.renderWeb.project,
				parameters.matchOwner,
				parameters.matchCollection,
				parameters.stack,
				parameters.maxAllowedErrorStitching,
				parameters.maxIterationsStitching,
				parameters.maxPlateauWidthStitching,
				parameters.blockOptimizerLambdas,
				parameters.blockOptimizerIterations,
				parameters.blockMaxPlateauWidth,
				parameters.blockMaxAllowedError,
				parameters.threadsWorker );
		try
		{
			w.run();
			for ( final SolveItemData< G, B, S > s : w.getSolveItemDataList() )
				s.visualizeAligned();

		} catch ( IOException | ExecutionException | InterruptedException
				| NoninvertibleModelException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		SimpleMultiThreading.threadHaltUnClean();
		*/

		// set up executor service
		LOG.info( "Multithreading with thread num="+ Runtime.getRuntime().availableProcessors() / 2 );

		final ExecutorService taskExecutor = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() / 2 );
		final ArrayList< Callable< List< SolveItemData< G, B, S > > > > tasks = new ArrayList<>();

		for ( final SolveItemData< G, B, S > solveItemData : this.solveSet.allItems() )
		{
			tasks.add( new Callable< List< SolveItemData< G, B, S > > >()
			{
				@Override
				public List< SolveItemData< G, B, S > > call() throws Exception
				{
					final DistributedSolveWorker< G, B, S > w = new DistributedSolveWorker<>(
							solveItemData,
							runParams.pGroupList,
							runParams.sectionIdToZMap,
							parameters.renderWeb.baseDataUrl,
							parameters.renderWeb.owner,
							parameters.renderWeb.project,
							parameters.matchOwner,
							parameters.matchCollection,
							parameters.stack,
							parameters.maxAllowedErrorStitching,
							parameters.maxIterationsStitching,
							parameters.maxPlateauWidthStitching,
							parameters.blockOptimizerLambdas,
							parameters.blockOptimizerIterations,
							parameters.blockMaxPlateauWidth,
							parameters.blockMaxAllowedError,
							parameters.threadsWorker );
					w.run();
	
					return w.getSolveItemDataList();
				}
			});
		}

		final ArrayList< SolveItemData< G, B, S > > allItems = new ArrayList<>();

		try
		{
			// invokeAll() returns when all tasks are complete
			final List< Future< List< SolveItemData< G, B, S > > > > futures = taskExecutor.invokeAll( tasks );

			for ( final Future< List< SolveItemData< G, B, S > > > future : futures )
				allItems.addAll( future.get() );
		}
		catch ( final Exception e )
		{
			IOFunctions.println( "Failed to compute alignments: " + e );
			e.printStackTrace();
			return null;
		}

		taskExecutor.shutdown();

		try
		{
			final GlobalSolve gs = globalSolve( allItems );

			LOG.info( "Took: " + ( System.currentTimeMillis() - time )/100 + " sec.");

			return gs;
		}
		catch ( NotEnoughDataPointsException | IllDefinedDataPointsException | InterruptedException | ExecutionException | NoninvertibleModelException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void main( String[] args )
	{
        final ClientRunner clientRunner = new ClientRunner(args) {
            @Override
            public void runClient(final String[] args) throws Exception {

                final ParametersDistributedSolve parameters = new ParametersDistributedSolve();

                // TODO: remove testing hack ...
                if (args.length == 0) {
                    final String[] testArgs = {
                            "--baseDataUrl", "http://tem-services.int.janelia.org:8080/render-ws/v1",
                            "--owner", "Z1217_19m",
                            "--project", "Sec08",
                            "--matchCollection", "Sec08_patch_matt",
                            "--stack", "v2_py_solve_03_affine_e10_e10_trakem2_22103_15758",
                            //"--targetStack", "v2_py_solve_03_affine_e10_e10_trakem2_22103_15758_new",
                            "--completeTargetStack",
                            
                            "--blockOptimizerLambdas", "1.0,0.5,0.1,0.01",
                            //"--blockOptimizerIterations", "100,100,40,20",
                            //"--blockMaxPlateauWidth", "50,50,50,50",

                            "--blockSize", "100",
                            //"--noStitching", // do not stitch first
                            
                            "--minZ", "10000",
                            "--maxZ", "10199",
                    };
                    parameters.parse(testArgs);
                } else {
                    parameters.parse(args);
                }

                LOG.info("runClient: entry, parameters={}", parameters);

                /*
                final DistributedSolve< RigidModel2D, InterpolatedAffineModel2D< AffineModel2D, RigidModel2D >, InterpolatedAffineModel2D< RigidModel2D, TranslationModel2D > > solve =
                		new DistributedSolveMultiThread<>(
                				new RigidModel2D(),
                				new InterpolatedAffineModel2D< AffineModel2D, RigidModel2D >( new AffineModel2D(), new RigidModel2D(), parameters.startLambda ),
                				new InterpolatedAffineModel2D< RigidModel2D, TranslationModel2D >( new RigidModel2D(), new TranslationModel2D(), 0.25 ),
                				parameters );
                */
               
                DistributedSolve.visualizeOutput = true;
                
                final DistributedSolve solve =
                		new DistributedSolveMultiThread(
                				parameters.globalModel(),
                				parameters.blockModel(),
                				parameters.stitchingModel(),
                				parameters );
               
                solve.run();
            }
        };
        clientRunner.run();
	}

	private static final Logger LOG = LoggerFactory.getLogger(DistributedSolveMultiThread.class);

}