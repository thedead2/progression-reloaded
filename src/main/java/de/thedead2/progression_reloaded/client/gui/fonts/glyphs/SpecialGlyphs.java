package de.thedead2.progression_reloaded.client.gui.fonts.glyphs;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Function;
import java.util.function.Supplier;

import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphInfo;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum SpecialGlyphs implements IUnbakedGlyph {
   WHITE(() -> generate(5, 8, (pX, pY) -> -1)),
   MISSING(() -> {
      int i = 5;
      int j = 8;
      return generate(i, j, (pX, pY) -> {
         boolean flag = pX == 0 || pX + 1 == i || pY == 0 || pY + 1 == j;
         return flag ? -1 : 0;
      });
   });

   final NativeImage image;

   private static NativeImage generate(int pWidth, int pHeight, SpecialGlyphs.PixelProvider pixelProvider) {
      NativeImage nativeimage = new NativeImage(NativeImage.Format.RGBA, pWidth, pHeight, false);

      for(int i = 0; i < pHeight; ++i) {
         for(int j = 0; j < pWidth; ++j) {
            nativeimage.setPixelRGBA(j, i, pixelProvider.getColor(j, i));
         }
      }

      nativeimage.untrack();
      return nativeimage;
   }

   SpecialGlyphs(Supplier<NativeImage> image) {
      this.image = image.get();
   }

   public float getAdvance() {
      return (float)(this.image.getWidth() + 1);
   }

   public BakedFontGlyph bake(Function<IGlyphInfo, BakedFontGlyph> function) {
      return function.apply(new IGlyphInfo() {
         public int getPixelWidth() {
            return SpecialGlyphs.this.image.getWidth();
         }

         public int getPixelHeight() {
            return SpecialGlyphs.this.image.getHeight();
         }

         public float getOverSample() {
            return 1.0F;
         }

         public void upload(int pXOffset, int pYOffset) {
            SpecialGlyphs.this.image.upload(0, pXOffset, pYOffset, false);
         }

         public boolean isColored() {
            return true;
         }
      });
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   interface PixelProvider {
      int getColor(int pX, int pY);
   }
}