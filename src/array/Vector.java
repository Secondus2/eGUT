package array;

/**
 * \brief Abstract class of useful vector functions.
 * 
 * <p>Both integer vectors (int[]) and real-number vectors (double[]) are
 * handled in this class. Splitting the class into two has been considered,
 * but this may cause confusion when converting from one to the other, or when
 * trying to use methods that take both as arguments.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class Vector
{
	/*************************************************************************
	 * SIMPLE INTEGER METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check if the given <b>vector</b> is composed of zeros.
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return boolean showing whether <b>vector</b> is all zeros (true) or 
	 * contains a non-zero (false).
	 */
	public static boolean isZero(int[] vector)
	{
		for ( int element : vector )
			if ( element != 0 )
				return false;
		return true;
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * <p>Note that the given <b>vector</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @param start int index of <b>vector</b> to start at (inclusive).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 * @return int[] array subset of <b>vector</b>: [start, start+1, ...,
	 * stop-2, stop-1]
	 */
	public static int[] subset(int[] vector, int start, int stop)
	{
		int[] out = new int[stop-start];
		for ( int i = 0; i < out.length; i++ )
			out[i] = vector[i + start];
		return out;
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * <p>Note that the given <b>vector</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @param indices int[] array of indices to use.
	 * @return integer[] array subset of <b>vector</b>.
	 */
	public static int[] subset(int[] vector, int[] indices)
	{
		int[] out = new int[indices.length];
		for ( int i = 0; i < out.length; i++ )
			out[i] = vector[indices[i]];
		return out;
	}
	
	/**
	 * \brief Reverse the order of a vector.
	 * 
	 * <p>Note that the given <b>vector</b> will be unaffected by this
	 * method.</p>
	 * 
	 * <p>See also {@link #reverse(int[] vector)}.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return int[] array with the same elements as <b>vector</b>, but in the
	 * opposite order. 
	 */
	public static int[] flip(int[] vector)
	{
		int i = vector.length;
		int[] out = new int[i];
		for ( int element : vector )
			out[--i] = element;
		return out;
	}
	
	/**
	 * \brief Set all elements of the given <b>vector</b> to the integer
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>setAll(copy(<b>vector</b>), <b>value</b>)</i> or
	 * <i>newInt(<b>vector</b>.length, <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @param value	int value to use.
	 * @return	Given <b>vector</b>, with all elements set to <b>value</b>.
	 */
	public static int[] setAll(int[] vector, int value)
	{
		for ( int i = 0; i < vector.length; i++ )
			vector[i] = value;
		return vector;
	}
	
	/**
	 * \brief Reset all elements of the given <b>vector</b> to zero.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reset(copy(<b>vector</b>))</i> or
	 * <i>zerosInt(<b>vector</b>.length)</i> 
	 * to preserve the original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return	Given <b>vector</b>, with all elements set to zero.
	 */
	public static int[] reset(int[] vector)
	{
		return setAll(vector, 0);
	}
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to 
	 * <b>value</b>.
	 * 
	 * @param n	Length of the vector to create.
	 * @param value	int value to use.
	 * @return	int[] array of length <b>n</b>, with all elements set to
	 * <b>value</b>.
	 */
	public static int[] vector(int n, int value)
	{
		int[] vector = new int[n];
		return setAll(vector, value);
	}
	
	/**
	 * \brief A new integer vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return	int[] array of length <b>n</b>, with all elements set to zero.
	 */
	public static int[] zeroInt(int n)
	{
		return vector(n, 0);
	}
	
	public static int[] range(int n)
	{
		int[] vector = new int[n];
		for ( int i = 0; i < n; i++ )
			vector[i] = i;
		return vector;
	}
	
	/**
	 * \brief Copy the <b>vector</b> given to a new int[] array.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return	int[] that is a copy of <b>vector</b>.
	 */
	public static int[] copy(int[] vector)
	{
		int[] v = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			v[i] = vector[i];
		return v;
	}
	
	/**
	 *  \brief Add a scalar <b>value</b> to every element of  a <b>vector</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>add(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @param value	int to add to every element of <b>vector</b>.
	 * @return	Given <b>vector</b>, with all elements greater by
	 * <b>value</b>.
	 */
	public static int[] add(int[] vector, int value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] += value;
		return vector;
	}
	
	/**
	 * \brief Multiply a <b>vector</b> by a scalar <b>value</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>times(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @param value	int scalar by which to multiply <b>vector</b>.
	 * @return	Given <b>vector</b> multiplied by the given <b>value</b>.
	 */
	public static int[] times(int[] vector, int value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] *= value;
		return vector;
	}
	
	/**
	 * \brief Changes the sign of every element in the given <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reverse(copy(<b>vector</b>))</i> to preserve the original state of
	 * <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static int[] reverse(int[] vector)
	{
		return times(vector, -1);
	}
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>vector</b>.
	 * 
	 * <p>E.g. the sum of the vector <i>(a, b)</i> is <i>a + b</i>.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return int sum of all elements in the vector.
	 */
	public static int sum(int[] vector)
	{
		int sum = 0;
		for ( int i = 0; i < vector.length; i++ ) 
			sum += vector[i];
		return sum;
	}
	
	/**
	 * \brief Calculates the sum of each element squared in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the normSquare of the vector <i>(a, b)</i> is
	 * <i>a<sup>2</sup> + b<sup>2</sup></i>.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return double sum of all elements in the vector.
	 */
	public static int normSquare(int[] vector)
	{
		return dotProduct(vector, vector);
	}
	
	/**
	 * \brief Euclidean norm of the given <b>vector</b>.
	 * 
	 * <p>E.g. the normEuclid of the vector <i>(a, b)</i> is
	 * <i>(a<sup>2</sup> + b<sup>2</sup> )<sup>1/2</sup></i>.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of integers.
	 * @return	double Euclidean norm of elements in <b>vector</b>.
	 */
	public static double normEuclid(int[] vector)
	{
		return Math.sqrt(normSquare(vector));
	}
	
	/*************************************************************************
	 * SIMPLE DOUBLE METHODS
	 ************************************************************************/
	
	/**
	 * \brief Check if the given <b>vector</b> is composed of zeros.
	 * 
	 * <p>If confident that numerical issues can be ignored, consider using
	 * {@link #isZero(double[] vector)} instead (slightly faster).</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @param tolerance double value for the absolute tolerance, i.e.
	 * <i>|x<sub>i</sub>|</i> <= tolerance will be accepted as close enough to
	 * zero (helps avoid numerical issues). 
	 * @return boolean showing whether <b>vector</b> is all zeros (true) or 
	 * contains a non-zero (false).
	 */
	public static boolean isZero(double[] vector, double tolerance)
	{
		for ( double element : vector )
			if ( Math.abs(element) > tolerance )
				return false;
		return true;
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * <p>Note that the given <b>vector</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @param start int index of <b>vector</b> to start at (inclusive).
	 * @param stop int index of <b>vector</b> to stop at (exclusive).
	 * @return double[] array subset of <b>vector</b>: [start, start+1, ...,
	 * stop-2, stop-1]
	 */
	public static double[] subset(double[] vector, int start, int stop)
	{
		double[] out = new double[stop-start];
		for ( int i = 0; i < out.length; i++ )
			out[i] = vector[i + start];
		return out;
	}
	
	/**
	 * \brief Take a subset of the given <b>vector</b> and return it as a new
	 * vector.
	 * 
	 * <p>Note that the given <b>vector</b> will be unaffected by this
	 * method.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @param indices int[] array of indices to use.
	 * @return double[] array subset of <b>vector</b>.
	 */
	public static double[] subset(double[] vector, int[] indices)
	{
		double[] out = new double[indices.length];
		for ( int i = 0; i < out.length; i++ )
			out[i] = vector[indices[i]];
		return out;
	}
	
	/**
	 * \brief Reverse the order of a vector.
	 * 
	 * <p>Note that the given <b>vector</b> will be unaffected by this
	 * method.</p>
	 * 
	 * <p>See also {@link #reverse(double[] vector)}.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double[] array with the same elements as <b>vector</b>, but in
	 * the opposite order. 
	 */
	public static double[] flip(double[] vector)
	{
		int i = vector.length;
		double[] out = new double[i];
		for ( double element : vector )
			out[--i] = element;
		return out;
	}
	
	/**
	 * \brief Check if the given <b>vector</b> is composed of zeros.
	 * 
	 * <p>Note that this method is vulnerable to numerical issues, i.e. values
	 * that are extremely close to zero but not equal because of rounding.
	 * Consider using {@link #isZero(double[] vector, double tolerance)}
	 * instead.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return boolean showing whether <b>vector</b> is all zeros (true) or 
	 * contains a non-zero (false).
	 */
	public static boolean isZero(double[] vector)
	{
		for ( double element : vector )
			if ( element != 0.0 )
				return false;
		return true;
	}
	
	/**
	 * \brief Set all elements of the given <b>vector</b> to the double
	 * <b>value</b> given.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>setAll(copy(<b>vector</b>), <b>value</b>)</i> or
	 * <i>newDbl(<b>vector</b>.length, <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @param value	double value to use.
	 * @return	Given <b>vector</b>, with all elements set to <b>value</b>.
	 */
	public static double[] setAll(double[] vector, double value)
	{
		for ( int i = 0; i < vector.length; i++ )
			vector[i] = value;
		return vector;
	}
	
	/**
	 * \brief Reset all elements of the given <b>vector</b> to zero.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reset(copy(<b>vector</b>))</i> or
	 * <i>zerosDbl(<b>vector</b>.length)</i> 
	 * to preserve the original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return	Given <b>vector</b>, with all elements set to zero.
	 */
	public static double[] reset(double[] vector)
	{
		return setAll(vector, 0.0);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, and all elements set to 
	 * <b>value</b>.
	 * 
	 * @param n	Length of the vector to create.
	 * @param value	double value to use.
	 * @return	double[] array of length <b>n</b>, with all elements set to
	 * <b>value</b>.
	 */
	public static double[] vector(int n, double value)
	{
		double[] vector = new double[n];
		return setAll(vector, value);
	}
	
	/**
	 * \brief A new double vector of length <b>n</b>, and all elements set to
	 * zero.
	 * 
	 * @param n	Length of the vector to create.
	 * @return	double[] array of length <b>n</b>, with all elements set to
	 * zero.
	 */
	public static double[] zerosDbl(int n)
	{
		return vector(n, 0.0);
	}
	
	/**
	 * \brief Copy the <b>vector</b> given to a new double[] array.
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return	double[] that is a copy of <b>vector</b>.
	 */
	public static double[] copy(double[] vector)
	{
		double[] v = new double[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			v[i] = vector[i];
		return v;
	}
	
	/**
	 * \brief Add a scalar <b>value</b> to every element of  a <b>vector</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>add(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @param value	double to add to every element of <b>vector</b>.
	 * @return	Given <b>vector</b>, with all elements greater by
	 * <b>value</b>.
	 */
	public static double[] add(double[] vector, double value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] += value;
		return vector;
	}
	
	/**
	 * \brief Multiply a <b>vector</b> by a scalar <b>value</b>. 
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>times(copy(<b>vector</b>), <b>value</b>)</i> to preserve the
	 * original state of <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @param value	double scalar by which to multiply <b>vector</b>.
	 * @return	Given <b>vector</b> multiplied by the given <b>value</b>.
	 */
	public static double[] times(double[] vector, double value)
	{
		for ( int i = 0; i < vector.length; i++ ) 
			vector[i] *= value;
		return vector;
	}
	
	/**
	 * \brief Changes the sign of every element in the given <b>vector</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>reverse(copy(<b>vector</b>))</i> to preserve the original state of
	 * <b>vector</b>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return	Given <b>vector</b>, where all elements have their sign
	 * changed.
	 */
	public static double[] reverse(double[] vector)
	{
		return times(vector, -1.0);
	}
	
	/**
	 * \brief Calculates the sum of all elements in the given <b>vector</b>.
	 * 
	 * <p>E.g. the sum of the vector <i>(a, b)</i> is <i>a + b</i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double sum of all elements in the vector.
	 */
	public static double sum(double[] vector)
	{
		double sum = 0.0;
		for ( int i = 0; i < vector.length; i++ ) 
			sum += vector[i];
		return sum;
	}
	
	/**
	 * \brief Calculates the sum of each element squared in the given
	 * <b>vector</b>.
	 * 
	 * <p>E.g. the normSquare of the vector <i>(a, b)</i> is
	 * <i>a<sup>2</sup> + b<sup>2</sup></i>.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double sum of all elements in the vector.
	 */
	public static double normSquare(double[] vector)
	{
		return dotProduct(vector, vector);
	}
	
	/**
	 * \brief Euclidean norm of the given <b>vector</b>.
	 * 
	 * <p>E.g. the normEuclid of the vector <i>(a, b)</i> is
	 * <i>(a<sup>2</sup> + b<sup>2</sup> )<sup>1/2</sup></i>.</p>
	 * 
	 * <p>Note that <b>vector</b> will be unaffected by this method.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return	double Euclidean norm of elements in <b>vector</b>.
	 */
	public static double normEuclid(double[] vector)
	{
		return Math.sqrt(normSquare(vector));
	}
	
	/**
	 * \brief Scale each element of the given <b>vector</b> by the same
	 * amount, so that the Euclidean norm of <b>vector</b> becomes 
	 * <b>newNorm</b>.
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>normaliseEuclid(copy(<b>vector</b>), <b>value</b>)</i> to preserve
	 * the original state of <b>vector</b>.</p>
	 * 
	 * <p>Note also that if the <b>vector</b> is composed of all zeros, this
	 * method will simply return the <b>vector</b> unchanged.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @param newNorm double value for the Euclidean norm of <b>vector</i>.
	 * @return double[] array of <b>vector</b>, where the elements have been
	 * scaled so that {@link #normEuclid(double[] vector)} = newNorm.
	 */
	public static double[] normaliseEuclid(double[] vector, double newNorm)
	{
		double oldNorm = normEuclid(vector);
		if ( oldNorm == 0.0 )
			return vector;
		return times(vector, newNorm/oldNorm);
	}
	
	/**
	 * \brief Scale each element of the given <b>vector</b> by the same
	 * amount, so that the Euclidean norm of <b>vector</b> becomes 1.0
	 * 
	 * <p>Note that <b>vector</b> will be overwritten; use
	 * <i>normaliseEuclid(copy(<b>vector</b>))</i> to preserve the original
	 * state of <b>vector</b>.</p>
	 * 
	 * <p>Note also that if the <b>vector</b> is composed of all zeros, this
	 * method will simply return the <b>vector</b> unchanged.</p>
	 * 
	 * @param vector One-dimensional array of doubles.
	 * @return double[] array of <b>vector</b>, where the elements have been
	 * scaled so that {@link #normEuclid(double[] vector)} = 1.0
	 */
	public static double[] normaliseEuclid(double[] vector)
	{
		return normaliseEuclid(vector, 1.0);
	}
	
	/*************************************************************************
	 * CONVERTING BETWEEN INT[] AND DOUBLE[]
	 ************************************************************************/
	
	/**
	 * \brief Recast a double[] as an int[].
	 * 
	 * <p>Note that any digits after the decimal point are simply discarded.
	 * See {@link #round(double[])}, etc for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the recast double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] toInt(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) vector[i];
		return out;
	}
	
	/**
	 * \brief Round a double[] as an int[].
	 * 
	 * <p>Note that elements of <b>vector</b> are rounded as in
	 * <i>Math.round(double x)</i>. See {@link #toDbl(double[])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the rounded double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] round(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) Math.round(vector[i]);
		return out;
	}
	
	/**
	 * \brief Floor a double[] as an int[].
	 * 
	 * <p>Note that elements of <b>vector</b> are floored as in
	 * <i>Math.floor(double x)</i>. See {@link #toDbl(double[])}, etc
	 * for alternate methods. This method should give identical output to
	 * <i>recastToInt()</i> when all elements of <b>vector</b> are 
	 * positive.</p>
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the floored double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] floor(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) Math.floor(vector[i]);
		return out;
	}
	
	/**
	 * \brief Ceiling a double[] as an int[].
	 * 
	 * <p>Note that elements of <b>vector</b> are ceilinged as in
	 * <i>Math.ceil(double x)</i>. See {@link #toDbl(double[])}, etc
	 * for alternate methods.</p>  
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	int[] array where each element is the ceilinged double in the
	 * corresponding position of <b>vector</b>.
	 */
	public static int[] ceil(double[] vector)
	{
		int[] out = new int[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (int) Math.ceil(vector[i]);
		return out;
	}
	
	/**
	 * \brief Recast an int[] as a double[].
	 * 
	 * <p>Note also that this method makes a copy, so the original state of 
	 * <b>vector</b> will be unaffected.</p>
	 * 
	 * @param vector One-dimensional array of doubles. 
	 * @return	double[] array where each element is the recast int in the
	 * corresponding position of <b>vector</b>.
	 */
	public static double[] toDbl(int[] vector)
	{
		double[] out = new double[vector.length];
		for ( int i = 0; i < vector.length; i++ )
			out[i] = (double) vector[i];
		return out;
	}
	
	
	/*************************************************************************
	 * TWO VECTOR METHODS
	 ************************************************************************/
	
	public static void checkLengths(int[] a, int[] b)
	{
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null vector given.");
		if ( a.length != b.length )
			throw new IllegalArgumentException("Vectors must be the same length.");
	}
	
	public static void checkLengths(double[] a, double[] b)
	{
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null vector given.");
		if ( a.length != b.length )
			throw new IllegalArgumentException("Vectors must be the same length.");
	}
	
	public static void checkLengths(double[] a, int[] b)
	{
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null vector given.");
		if ( a.length != b.length )
			throw new IllegalArgumentException("Vectors must be the same length.");
	}
	
	/**
	 * \brief Add two vectors together.
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(int[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a One-dimensional array of integers (overwritten).
	 * @param b One-dimensional array of integers (preserved).
	 * @return int[] array of <b>a</b> + <b>b</b>.
	 */
	public static int[] add(int[] a, int[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ ) 
			a[i] += b[i];
		return a;
	}
	
	/**
	 * \brief Add two vectors together.
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(double[] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return double[] array of <b>a</b> + <b>b</b>.
	 */
	public static double[] add(double[] a, double[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ ) 
			a[i] += b[i];
		return a;
	}
	
	/**
	 * \brief Add two vectors together.
	 * 
	 * <p>Note that <b>a</b> will be overwritten; use 
	 * <i>add({@link #copy(double[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * <p>Not also that <i>a + b = b + a</i>. The result is returned as a
	 * double. Returning an int can be done safely using: <i>(int)
	 * add(a, b)</i> or <i>add(</i>{@link #toInt(double[] a)}<i>, b)</i>.</p>
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of integers (preserved).
	 * @return double[] array of <b>a</b> + <b>b</b>.
	 */
	public static double[] add(double[] a, int[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ ) 
			a[i] += b[i];
		return a;
	}
	
	/**
	 * \brief Times two vectors together element-wise.
	 * 
	 * <p>Note that this method of multiplying differs from the dot product
	 * and the cross product.</p>
	 * 
	 * <p>Note also that <b>a</b> will be overwritten; use 
	 * <i>times({@link #copy(int[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a One-dimensional array of integers (overwritten).
	 * @param b One-dimensional array of integers (preserved).
	 * @return int[] array of <b>a</b> times <b>b</b>.
	 */
	public static int[] times(int[] a, int[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ ) 
			a[i] *= b[i];
		return a;
	}
	
	/**
	 * \brief Times two vectors together element-wise.
	 * 
	 * <p>Note that this method of multiplying differs from the dot product
	 * and the cross product.</p>
	 * 
	 * <p>Note also that <b>a</b> will be overwritten; use 
	 * <i>times({@link #copy(double[] a)}, <b>b</b>)</i> to preserve the
	 * original state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of doubles (preserved).
	 * @return double[] array of <b>a</b> times <b>b</b>.
	 */
	public static double[] times(double[] a, double[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ ) 
			a[i] *= b[i];
		return a;
	}
	
	/**
	 * \brief Times two vectors together element-wise.
	 * 
	 * <p>Note that this method of multiplying differs from the dot product
	 * and the cross product.</p>
	 * 
	 * <p>Note also that <b>a</b> will be overwritten; use 
	 * <i>times({@link #copy(int[] a)}, <b>b</b>)</i> to preserve the original
	 * state of <b>a</b>. <b>b</b> will be unaffected.</p>
	 * 
	 * <p>furthermore, the result is returned as a double. Returning an
	 * int is potentially ambiguous: <i>(int) times(a, b)</i> will often
	 * be different to <i>times(</i>{@link #toInt(double[] a)}<i>, b)</i>!</p>
	 * 
	 * @param a One-dimensional array of doubles (overwritten).
	 * @param b One-dimensional array of integers (preserved).
	 * @return double[] array of <b>a</b> times <b>b</b>.
	 */
	public static double[] times(double[] a, int[] b)
	{
		checkLengths(a, b);
		for ( int i = 0; i < a.length; i++ ) 
			a[i] *= b[i];
		return a;
	}
	
	/**
	 * \brief Calculate the dot product of the two vectors given.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> ).(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>*b<sub>1</sub> +
	 * a<sub>2</sub>*b<sub>2</sub></i></p>
	 * 
	 * <p>Note that neither vector will be unaffected by this method,
	 * and that the order they are entered as arguments is irrelevant.</p>
	 * 
	 * @param a One-dimensional array of integers.
	 * @param b One-dimensional array of integers.
	 * @return int value of the dot product of <b>a</b> and <b>b</b>.
	 */
	public static int dotProduct(int[] a, int[] b)
	{
		checkLengths(a, b);
		int out = 0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] * b[i];	
		return out;
	}
	
	/**
	 * \brief Calculate the dot product of the two vectors given.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> ).(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>*b<sub>1</sub> +
	 * a<sub>2</sub>*b<sub>2</sub></i></p>
	 * 
	 * <p>Note that neither vector will be unaffected by this method,
	 * and that the order they are entered as arguments is irrelevant.</p>
	 * 
	 * @param a One-dimensional array of doubles.
	 * @param b One-dimensional array of doubles.
	 * @return double value of the dot product of <b>a</b> and <b>b</b>.
	 */
	public static double dotProduct(double[] a, double[] b)
	{
		checkLengths(a, b);
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] * b[i];	
		return out;
	}
	
	/**
	 * \brief Calculate the dot product of the two vectors given.
	 * 
	 * <p>For example, <i>(a<sub>1</sub> , a<sub>2</sub> ).(b<sub>1</sub> ,
	 * b<sub>2</sub> ) = a<sub>1</sub>*b<sub>1</sub> +
	 * a<sub>2</sub>*b<sub>2</sub></i></p>
	 * 
	 * <p>Note that neither vector will be unaffected by this method,
	 * and that <i>a</i>.<i>b</i> = <i>b</i>.<i>a</i>.</p>
	 * 
	 * <p>Note also that the result is returned as a double. Returning an
	 * int is potentially ambiguous: <i>(int) dotProduct(a, b)</i> will often
	 * be different to <i>dotProduct(</i>{@link #toInt(double[] a)}<i>, b)</i>
	 * !</p>
	 * 
	 * @param a One-dimensional array of doubles.
	 * @param b One-dimensional array of integers.
	 * @return double value of the dot product of <b>a</b> and <b>b</b>.
	 */
	public static double dotProduct(double[] a, int[] b)
	{
		checkLengths(a, b);
		double out = 0.0;
		for ( int i = 0; i < a.length; i++ )
			out += a[i] * b[i];	
		return out;
	}
}