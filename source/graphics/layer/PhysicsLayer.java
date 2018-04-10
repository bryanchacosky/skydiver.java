package com.bryanchacosky.core.graphics.layer;

import playn.core.Image;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.gl.ImageLayerGL;

import com.bryanchacosky.core.SkyDiver;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Image layer subclass which includes physics properties, such as acceleration and velocity.
 *
 * @author Bryan Chacosky
 */
public class PhysicsLayer extends ImageLayerGL implements com.bryanchacosky.core.graphics.Layer
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Default vertical acceleration, representing gravity = {@value #DefaultVerticalAcceleration} */
  public static final float DefaultVerticalAcceleration = 300.0f;

  /** Default horizontal acceleration = {@value #DefaultHorizontalAcceleration} */
  public static final float DefaultHorizontalAcceleration = 0.0f;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Vertical acceleration.  Typically this would be implemented with a constant but we're going to support
   * adjustments to simulate drag from the parachute. */
  private float v_acceleration = 0.0f;

  /** Horizontal acceleration. */
  private float h_acceleration = 0.0f;

  /** Vertical velocity. */
  private float v_velocity = 0.0f;

  /** Horizontal velocity. */
  private float h_velocity = 0.0f;

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Instantiates a new physics layer.
   *
   * @param image - Image for the image layer.
   */
  public PhysicsLayer( final Image image )
  {
    super( PlayN.graphics( ).ctx( ) );
    super.setImage( image );
    super.setSize( image.width( ), image.height( ) );

    // Initialize accelerations:
    this.resetVerticalAcceleration( );
    this.resetHorizontalAcceleration( );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Resets the vertical acceleration to default.  See {@link #DefaultVerticalAcceleration}.
   */
  public void resetVerticalAcceleration( )
  {
    this.setVerticalAcceleration( PhysicsLayer.DefaultVerticalAcceleration );
  }

  /**
   * Sets a custom vertical acceleration in units per second^2.
   * Default value is {@link #DefaultVerticalAcceleration}.
   *
   * @param acceleration - Acceleration.
   */
  public void setVerticalAcceleration( final float acceleration )
  {
    this.v_acceleration = acceleration;
  }

  /**
   * Resets the horizontal acceleration to default.  See {@link #DefaultHorizontalAcceleration}.
   */
  public void resetHorizontalAcceleration( )
  {
    this.setHorizontalAcceleration( PhysicsLayer.DefaultHorizontalAcceleration );
  }

  /**
   * Sets a custom horizontal acceleration in units per second^2.
   * Default value is {@link PhysicsLayer#DefaultHorizontalAcceleration}.
   *
   * @param acceleration - Acceleration.
   */
  public void setHorizontalAcceleration( final float acceleration )
  {
    this.h_acceleration = acceleration;
  }

  /**
   * Sets the vertical velocity in units per seconds.
   *
   * @param velocity - Velocity.
   */
  public void setVerticalVelocity( final float velocity )
  {
    this.v_velocity = velocity;
  }

  /**
   * Sets the horizontal velocity in units per seconds.
   *
   * @param velocity - Velocity.
   */
  public void setHorizontalVelocity( final float velocity )
  {
    this.h_velocity = velocity;
  }

  /**
   * Clears all accelerations and velocities on the layer.
   */
  public void clearMovement( )
  {
    this.setVerticalAcceleration( 0.0f );
    this.setHorizontalAcceleration( 0.0f );
    this.setVerticalVelocity( 0.0f );
    this.setHorizontalVelocity( 0.0f );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Gets the current vertical velocity.
   *
   * @return Current vertical velocity.
   */
  public float getVerticalVelocity( )
  {
    return this.v_velocity;
  }

  /**
   * Gets the current horizontal velocity.
   *
   * @return Current horizontal velocity.
   */
  public float getHorizontalVelocity( )
  {
    return this.h_velocity;
  }

  /**
   * Returns the magnitude of the object's current combined velocity (vertical and horizontal).
   *
   * @return Magnitude of the current velocity.
   */
  public double getCurrentVelocity( )
  {
    return Math.sqrt( this.h_velocity * this.h_velocity + this.v_velocity * this.v_velocity );
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns true if this layer intersects with the passed layer.
   *
   * @param layer - Layer to compare intersection against.
   * @return True if the two layers are intersecting, otherwise false.
   */
  public boolean intersects( final Layer.HasSize layer )
  {
    return this.intersects( layer.transform( ).tx( ), layer.transform( ).ty( ), layer.width( ), layer.height( ) );
  }

  /**
   * Returns true if this layer intersects with the bounding area representing by a tx, ty, width, and height properties of a layer.
   *
   * @param tx - Transform x position of the layer.
   * @param ty - Transform y position of the layer.
   * @param width - Width of the layer.
   * @param height - Height of the layer.
   * @return True if the two layers are intersecting, otherwise false.
   */
  public boolean intersects( final float tx, final float ty, final float width, final float height )
  {
    if ( this.transform.tx( ) > tx + width )           return false;
    if ( this.transform.tx( ) + this.width( ) < tx )   return false;
    if ( this.transform.ty( ) > ty + height )          return false;
    if ( this.transform.ty( ) + this.height( ) < ty )  return false;

    return true;
  }

  /**
   * Returns true if the layer is partially or fully contained within the window, otherwise false.
   *
   * @return True if the layer is partially or fully contained within the window, otherwise false.
   */
  public boolean isOnscreen( )
  {
    if ( this.transform.tx( ) + this.width( ) < 0.0f )    return false;
    if ( this.transform.tx( ) > SkyDiver.WindowWidth )    return false;
    if ( this.transform.ty( ) + this.height( ) < 0.0f )   return false;
    if ( this.transform.ty( ) > SkyDiver.WindowHeight )   return false;

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void update( final float delta )
  {
    // Divide the delta duration by 1000 since its easier for the user to provide values in units per second, not units per millisecond:
    final float dt = delta / 1000.0f;

    // Integrate the acceleration to update the velocity with basic Euler integration:
    this.v_velocity += this.v_acceleration * dt;
    this.h_velocity += this.h_acceleration * dt;

    // Update the translation based off the current velocity:
    final float x = this.transform( ).tx( ) + this.h_velocity * dt;
    final float y = this.transform( ).ty( ) + this.v_velocity * dt;
    this.setTranslation( x, y );
  }

  @Override
  public void paint( final float alpha )
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////