/**
 * Phase 2 AE2 GUI theme mixins will live in this package.
 * <p>
 * Planned injection points (via {@link com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider}):
 * <ul>
 *   <li>{@code AE2Button} sprite resolution</li>
 *   <li>{@code VerticalButtonBar} toolbar background sprite</li>
 *   <li>{@code Icon.getBlitter} states atlas path</li>
 *   <li>{@code BackgroundGenerator} tiled background texture</li>
 *   <li>{@code ScreenStyle.getColor} palette override when theming AE2 native screens</li>
 * </ul>
 * Phase 1 intentionally ships with an empty mixin config so AE2 vanilla assets remain untouched.
 */
package com.knightcode.appliedstoragesorter.mixin;
