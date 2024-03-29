/**
 * \package utils
 * \brief Package of classes that perform utility functions in the process of running an iDynoMiCS Simulation
 * 
 * Package of classes that perform utility functions in the process of running an iDynoMiCS Simulation. This package is part of iDynoMiCS v1.2, governed by the 
 * CeCILL license under French law and abides by the rules of distribution of free software.  You can use, modify and/ or redistribute 
 * iDynoMiCS under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL  "http://www.cecill.info".
 */
package utility;

/**
 * \brief Class for storing complex numbers as two doubles representing real
 * and imaginary part, respectively.
 * 
 * Includes basic math operations (addition, subtraction, multiplication,
 * division). 
 *
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre
 * for Infection Research (Germany).
 */
public class Complex
{	
	/**
	 * 'Real' part of this complex number
	 */
	private Double _real;
	
	/**
	 * 'Imaginary' part of this complex number
	 */
	private Double _imag;
	
	/**
	 * \brief Default constructor. Sets number to 0 + 0i
	 */
	public Complex()
	{
		this._real = 0.0;
		this._imag = 0.0;
	}
	
	/**
	 * \brief Specific constructor, setting number to given real and imaginary
	 * part.
	 * 
	 * @param realPart	Number to be assigned to the 'Real' part of this
	 * complex number.
	 * @param imaginaryPart	Number to be assigned to the 'Imaginary' part of
	 * this complex number.
	 */
	public Complex(Double realPart, Double imaginaryPart)
	{
		this._real = realPart;
		this._imag = imaginaryPart;
	}
	
	/**
	 * \brief Utility to add a complex number c to this complex number.
	 * 
	 * @param c	Complex number being added to this complex number.
	 */
	public void add(Complex c)
	{
		this._real += c.getReal();
		this._imag += c.getImag();
	}
	
	/**
	 * \brief Utility to subtract a complex number c from this complex number.
	 * 
	 * @param c	Complex number being subtracted from this complex number.
	 */
	public void sub(Complex c)
	{
		this._real -= c.getReal(); 
		this._imag -= c.getImag();
	}
	
	/**
	 * \brief Utility to multiply a complex number c to this complex number.
	 * 
	 * @param c	Complex number being multiplied to this complex number.
	 */
	public void mul(Complex c)
	{
		Double re = this._real;
		Double im = this._imag;
		this._real = re * c.getReal() - im * c.getImag();
		this._imag = re * c.getImag() + im * c.getReal();
	}
	
	/**
	 * \brief Utility to divide a complex number by complex number c.
	 * 
	 * @param c	Complex number by which this object is being divided .
	 */
	public void div(Complex c)
	{
		Double re = this._real;
		Double im = this._imag;
		Double  d = c.getReal() * c.getReal() + c.getImag() * c.getImag();
		this._real = (re * c.getReal() + im * c.getImag())/d;
		this._imag = (im * c.getReal() - re * c.getImag())/d;
	}

	/**
	 * \brief Utility to add a double to this complex number.
	 * 
	 * @param z	Double being added to this number.
	 */
	public void add(Double z)
	{
		this._real += z;
	}
	
	/**
	 * \brief Utility to subtract a double from this complex number.
	 * 
	 * @param z	Double being subtracted from this number.
	 */
	public void sub(Double z)
	{
		this._real -= z; 
	}
	
	/**
	 * \brief Utility to multiply this complex number by a double.
	 * 
	 * @param z	Double being multiplied to this complex number.
	 */
	public void mul(Double z)
	{
		this._real *= z;
		this._imag *= z;
	}
	
	/**
	 * \brief Utility to divide this complex number by a double.
	 * 
	 * @param z	Double being divided into this complex number.
	 */
	public void div(Double z)
	{
		this._real /= z;
		this._imag /= z;
	}
	
	/**
	 * \brief Sets the real part of this number.
	 * 
	 * @param realPart	Double value to set the real part of this number to.
	 */
	public void setReal(Double realPart)
	{
		this._real = realPart;
	}

	/**
	 * \brief Sets the imaginary part of this number.
	 * 
	 * @param imaginaryPart	Double value to set the imaginary part of this
	 * number to.
	 */
	public void setImag(Double imaginaryPart)
	{
		this._imag = imaginaryPart;
	}
	
	/**
	 * \brief Returns the real part of this number.
	 * 
	 * @return Double value stating the value of the real part of this complex
	 * number.
	 */
	public Double getReal()
	{
		return this._real;
	}
	
	/**
	 * \brief Returns the imaginary part of this number.
	 * 
	 * @return Double value stating the value of the imaginary part of this
	 * complex number.
	 */
	public Double getImag()
	{
		return this._imag;
	}
	
	/**
	 * 
	 * @return
	 */
	public Boolean isReal()
	{
		return (this._imag == 0.0);
	}
}
