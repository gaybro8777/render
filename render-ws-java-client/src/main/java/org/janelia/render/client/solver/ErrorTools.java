package org.janelia.render.client.solver;

import java.util.HashMap;

import org.janelia.render.client.solver.DistributedSolve.GlobalSolve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.util.BdvStackSource;
import mpicbg.util.RealSum;
import net.imglib2.util.Pair;

public class ErrorTools
{
	public static void errorAnalysis( final GlobalSolve gs, final double significance, final int numThreads )
	{
		final RealSum avgErrorSum = new RealSum();

		for ( final String tileId : gs.idToTileSpecGlobal.keySet() )
			avgErrorSum.add( SolveItemData.avgError( gs.idToErrorMapGlobal.get( tileId ) ) );

		final double avgError = avgErrorSum.getSum() / gs.idToTileSpecGlobal.keySet().size();

		final RealSum stDevErrorSum = new RealSum();

		for ( final String tileId : gs.idToTileSpecGlobal.keySet() )
			stDevErrorSum.add( Math.pow( SolveItemData.avgError( gs.idToErrorMapGlobal.get( tileId ) ) - avgError, 2 ) );

		final double stDev = Math.sqrt( stDevErrorSum.getSum() / gs.idToTileSpecGlobal.keySet().size() );

		final HashMap<String, Float> idToMinError = new HashMap<>();
		final HashMap<String, Float> idToAvgError = new HashMap<>();
		final HashMap<String, Float> idToMaxError = new HashMap<>();
		final HashMap<String, Float> idToRegion = new HashMap<>();

		for ( final String tileId : gs.idToTileSpecGlobal.keySet() )
		{
			final double minErr = SolveItemData.minError( gs.idToErrorMapGlobal.get( tileId ) );
			final double avgErr = SolveItemData.avgError( gs.idToErrorMapGlobal.get( tileId ) );
			final double maxErr = SolveItemData.maxError( gs.idToErrorMapGlobal.get( tileId ) );

			idToMinError.put( tileId, (float)minErr );
			idToAvgError.put( tileId, (float)avgErr );
			idToMaxError.put( tileId, (float)maxErr );

			if ( avgErr > avgError + significance * stDev )
				idToRegion.put( tileId, 1.0f );
			else
				idToRegion.put( tileId, 0.0f );
		}

		BdvStackSource< ? > source = VisualizeTools.visualizeMultiRes(
				gs.idToFinalModelGlobal, gs.idToTileSpecGlobal, idToAvgError, 1, 128, 2, numThreads );

		source = VisualizeTools.visualizeMultiRes(
				source, gs.idToFinalModelGlobal, gs.idToTileSpecGlobal, idToMinError, 1, 128, 2, numThreads );
		
		VisualizeTools.visualizeMultiRes(
				source, gs.idToFinalModelGlobal, gs.idToTileSpecGlobal, idToMaxError, 1, 128, 2, numThreads );
	}

	public static void errorVisualization( final GlobalSolve gs, final int numThreads )
	{
		double minError = Double.MAX_VALUE;
		RealSum avgError = new RealSum();
		double maxError = -Double.MAX_VALUE;
		String maxTileId = "";

		final HashMap<String, Float> idToValue = new HashMap<>();
		for ( final String tileId : gs.idToTileSpecGlobal.keySet() )
		{
			final double error = SolveItemData.avgError( gs.idToErrorMapGlobal.get( tileId ) );
			idToValue.put( tileId, (float)error );
			minError = Math.min( minError, error );
			avgError.add( error );

			if ( error > maxError )
			{
				maxTileId = tileId;
				maxError = error;
			}

			//idToValue.put( tileId, gs.zToDynamicLambdaGlobal.get( (int)Math.round( gs.idToTileSpecGlobal.get( tileId ).getZ() ) ).floatValue() + 1 ); // between 1 and 1.2
		}

		BdvStackSource< ? > vis = VisualizeTools.visualizeMultiRes(
				gs.idToFinalModelGlobal, gs.idToTileSpecGlobal, idToValue, 1, 128, 2, numThreads );

		LOG.info( "Min err=" + minError + ", avg err=" + (avgError.getSum()/gs.idToTileSpecGlobal.keySet().size()) + ", max err=" + maxError  + " (" + maxTileId + ")" );
		for ( final Pair< String, Double > error : gs.idToErrorMapGlobal.get( maxTileId ) )
			LOG.info( error.getA() + ": " + error.getB() );

		vis.setDisplayRange( 0, maxError );
		vis.setDisplayRangeBounds( 0, maxError );
	}

	private static final Logger LOG = LoggerFactory.getLogger(ErrorTools.class);
}