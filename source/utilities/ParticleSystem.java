package com.bryanchacosky.core.utilities;

import java.util.Random;

import playn.core.GroupLayer;
import playn.core.ImmediateLayer.Renderer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Surface;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * A very simplified particle system that shoots particles in a randomized direction.
 *
 * @author Bryan Chacosky
 */
public abstract class ParticleSystem
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Attaches the particle system to the layer and fires the particles.
   *
   * @param layer - Layer.
   */
  public void fire( final GroupLayer layer, final float x, final float y )
  {
    for ( int i = 0; i != this.getParticleCount( ); ++i )
    {
      // Create the particle layer and attach it to the parent layer:
      final Layer particle = this.createParticle( );
      particle.setTranslation( x, y );
      layer.add( particle );

      // Pick a randomized direction to shoot the particle into:
      final double angle  = new Random( ).nextDouble( ) * ( 2.0 * Math.PI );
      final double length = new Random( ).nextInt( 50 ) + 25;

      // Shoot the particle in the direction:
      Animator.lerp( particle, ( float )( x + Math.cos( angle ) * length ), ( float )( y + Math.sin( angle ) * length ), this.getParticleDuration( ), new Animator.Callback( )
      {
        @Override
        public void onAnimationComplete( )
        {
          /*
           * I had been using 'particle.destroy( );' instead of setting invisible but this
           * cause inconsistent issues with destroying the layer, since it would occassionally would
           * destroy the layer while OpenGL is painting ... so I would get concurrent modification
           * exceptions.  Making invisible isn't the best solution but it's more reliable.
           */
          particle.setVisible( false );
        }
      });
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Required methods:

  /**
   * Returns the number of particles to generate.
   * This method is called once per firing.
   *
   * @return Particle count.
   */
  protected abstract int getParticleCount( );

  /**
   * Returns the lifespan of a particle, in milliseconds.
   * This method is called once per firing.
   *
   * @return Particle duration
   */
  protected abstract long getParticleDuration( );

  /**
   * Returns the particle color.
   * This method is called per-particle.
   *
   * @return Particle color
   */
  protected abstract int getParticleColor( );

  /**
   * Returns the particle size.
   * This method is called per-particle.
   *
   * @return Particle size
   */
  protected abstract int getParticleSize( );

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates an individual particle.
   *
   * @return Particle layer.
   */
  private Layer createParticle( )
  {
    final int size  = this.getParticleSize( );
    final int color = this.getParticleColor( );

    return PlayN.graphics( ).createImmediateLayer( size, size, new Renderer( )
    {
      @Override
      public void render( final Surface surface )
      {
        surface.setFillColor( color );
        surface.fillRect( 0, 0, size, size );
      }
    });
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////