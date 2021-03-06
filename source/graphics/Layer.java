package com.bryanchacosky.core.graphics;


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Custom extension to {@link playn.core.Layer}.
 *
 * @author Bryan Chacosky
 */
public interface Layer extends playn.core.Layer
{
  /**
   * Updates the renderable object.
   *
   * @param delta - Delta of the last frame.
   */
  public void update( float delta );

  /**
   * Render the renderable object.
   *
   * @param alpha - Renderable alpha.
   */
  public void paint( float alpha );
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////