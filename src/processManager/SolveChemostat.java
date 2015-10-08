package processManager;

import java.util.HashMap;
import java.util.LinkedList;

import agent.Agent;
import agent.state.HasReactions;
import boundary.Boundary;
import boundary.ChemostatConnection;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import solver.ODErosenbrock;
import utility.ExtraMath;
import utility.LogFile;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public class SolveChemostat extends ProcessManager
{
	/**
	 * TODO Could let the user choose which ODEsolver to use, if we ever get
	 * around to implementing more.
	 */
	protected ODErosenbrock _solver;
	
	/**
	 * TODO
	 */
	protected String[] _soluteNames;
	
	/**
	 * 
	 */
	protected HashMap<String, Double> _inflow;
	
	/**
	 * Dilution rate in units of time<sup>-1</sup>.
	 */
	protected double _dilution;
	
	
	protected LinkedList<ChemostatConnection> _inConnections = 
										new LinkedList<ChemostatConnection>();
	
	protected LinkedList<ChemostatConnection> _outConnections = 
										new LinkedList<ChemostatConnection>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 *
	 */
	public SolveChemostat()
	{
		
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		this._solver = new ODErosenbrock();
		this._solver.init(this._soluteNames, false, 1.0e-6, 1.0e-6);
		this._inflow = new HashMap<String, Double>();
		for ( String sName : this._soluteNames )
			this._inflow.put(sName, 0.0);
		this._dilution = 0.0;
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param inflow
	 * @param dilution
	 */
	public void setInflow(HashMap<String, Double> inflow)
	{
		this._inflow = inflow;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param dilution
	 */
	public void setDilution(double dilution)
	{
		this._dilution = dilution;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param boundaries
	 */
	@Override
	public void showBoundaries(LinkedList<Boundary> boundaries)
	{
		ChemostatConnection aChemoConnect;
		for ( Boundary aBoundary : boundaries )
			if ( aBoundary instanceof ChemostatConnection )
			{
				aChemoConnect = (ChemostatConnection) aBoundary;
				if ( aChemoConnect.getFlowRate() > 0.0 )
					this._inConnections.add(aChemoConnect);
				else
					this._outConnections.add(aChemoConnect);
			}
		/*
		 * Update the dilution rate now to check that the outflow matches the
		 * inflow.
		 */
		this.updateDilutionInflow();
	}
	
	
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 */
	protected void updateDilutionInflow()
	{
		double inRate = 0.0, outRate = 0.0;
		for ( String sName : this._soluteNames )
			this._inflow.put(sName, 0.0);
		for ( ChemostatConnection aChemoConnect : this._inConnections )
		{
			inRate += aChemoConnect.getFlowRate();
			for ( String sName : this._soluteNames )
			{
				double temp = aChemoConnect.getConcentrations().get(sName);
				this._inflow.put(sName, this._inflow.get(sName)+temp);
			}
		}
		for ( ChemostatConnection aChemoConnect : this._outConnections )
			outRate -= aChemoConnect.getFlowRate();
		if ( inRate != outRate )
		{
			throw new IllegalArgumentException(
							"Chemostat inflow and outflow rates must match!");
		}
		this._dilution = inRate;
	}
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		this.updateDilutionInflow();
		/*
		 * Update the solver's 1st derivative function (dY/dT).
		 */
		this._solver.set1stDeriv( (double[] y) ->
		{
			/*
			 * First deal with inflow and dilution: dYdT = D(Sin - S)
			 */
			double[] dYdT = Vector.reverse(Vector.copy(y));
			HashMap<String,Double> concns = new HashMap<String,Double>();
			for ( int i = 0; i < this._soluteNames.length; i++ )
			{
				dYdT[i] += this._inflow.get(this._soluteNames[i]);
				concns.put(this._soluteNames[i], y[i]);
			}
			Vector.times(dYdT, this._dilution);
			/*
			 * Apply agent reactions. Note that any agents without reactions
			 * will return an empty list of States, and so will be skipped.
			 */
			HasReactions aReacState;
			HashMap<String,Double> temp;
			for ( Agent agent : agents.getAllAgents() )
				for (Object aState : agent.getStates(HasReactions.tester))
				{
					aReacState = (HasReactions) aState;
					temp = aReacState.get1stTimeDerivatives(concns);
					for ( int i = 0; i < this._soluteNames.length; i++ )
						dYdT[i] += temp.get(this._soluteNames[i]);
				}
			/*
			 * TODO Apply extracellular reactions.
			 */
			/*System.out.println("\tS -> dYdT:"); //Bughunt
			for ( int i = 0; i < y.length; i++ )
				System.out.println("\t"+y[i]+"->"+dYdT[i]);*/
			return dYdT;
		});
		/*
		 * TODO Update the solver's 2nd derivative function (dF/dT)?
		 * Leave out if we don't want to do this for reactions (the solver
		 * will estimate dFdT numerically)
		 */
		this._solver.set2ndDeriv( (double[] y) ->
		{
			double[] dFdT = Vector.copy(y);
			for ( int i = 0; i < this._soluteNames.length; i++ )
				dFdT[i] -= this._inflow.get(this._soluteNames[i]);
			Vector.times(dFdT, ExtraMath.sq(this._dilution));
			/*
			 * TODO Apply agent reactions
			 */
			//for ( Agent agent : agents.getAllAgents() )
			//	agent.
			/*
			 * TODO Apply extracellular reactions.
			 */
			return dFdT;
		});
		/*
		 * Update the solver's Jacobian function (dF/dY).
		 */
		this._solver.setJacobian( (double[] y) ->
		{
			/*
			 * First deal with dilution: dYdY = -D
			 */
			double[][] jac = Matrix.identityDbl(y.length);
			Matrix.times(jac, -this._dilution);
			/*
			 * TODO Apply agent reactions
			 */
			//for ( Agent agent : agents.getAllAgents() )
			//	agent.
			/*
			 * TODO Apply extracellular reactions.
			 */
			
			return jac;
		});
		/*
		 * Finally, solve the system.
		 */
		double[] y = getY(environment);
		try { y = this._solver.solve(y, this._timeStepSize); }
		catch ( Exception e) { e.printStackTrace();}
		updateSolutes(environment, y);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @return
	 */
	protected double[] getY(EnvironmentContainer environment)
	{
		double[] y = Vector.zerosDbl(this._soluteNames.length);
		SpatialGrid sg;
		for ( int i = 0; i < y.length; i++ )
		{
			sg = environment.getSoluteGrid(this._soluteNames[i]);
			//TODO Use average?
			y[i] = sg.getMax(SpatialGrid.concn);
		}
		return y;
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO This may need to be updated now that solutes belong to the
	 * environment container
	 * 
	 * @param solutes
	 * @param y
	 */
	protected void updateSolutes(EnvironmentContainer environment, double[] y)
	{
		for ( int i = 0; i < y.length; i++ )
		{
			environment.getSoluteGrid(this._soluteNames[i]).setAllTo(
											SpatialGrid.concn, y[i], true);
		}
	}
}
