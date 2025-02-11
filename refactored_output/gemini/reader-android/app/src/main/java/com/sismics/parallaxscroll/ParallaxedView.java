```java
import java.lang.ref.WeakReference;
import java.util.List;

public abstract class ParallaxedView {
    protected WeakReference<View> view;
    protected int lastOffset;

    public ParallaxedView(View view) {
        this.lastOffset = 0;
        this.view = new WeakReference<>(view);
    }

    public boolean is(View v) {
        return (v != null && view != null && view.get() != null && view.get().equals(v));
    }

    public void setOffset(float offset) {
        View view = this.view.get();
        if (view != null) {
            translate(view, offset);
        }
    }

    public void setView(View view) {
        this.view = new WeakReference<>(view);
    }

    abstract protected void translate(View view, float offset);
}

class AlphaManipulator {
    private List<Animation> animations;

    public AlphaManipulator() {
        this.animations = new ArrayList<>();
    }

    public void setAlpha(View view, float alpha) {
        if (view != null) {
            animate(view, alpha);
        }
    }

    public synchronized void addAnimation(Animation animation) {
        animations.add(animation);
    }

    protected synchronized void animateNow() {
        View view = this.view.get();
        if (view != null) {
            AnimationSet set = new AnimationSet(true);
            for (Animation animation : animations) {
                if (animation != null) {
                    set.addAnimation(animation);
                }
            }
            set.setDuration(0);
            set.setFillAfter(true);
            view.setAnimation(set);
            set.start();
            animations.clear();
        }
    }

    abstract protected void animate(View view, float alpha);
}

class AndroidParallax extends ParallaxedView {
    public AndroidParallax(View view) {
        super(view);
    }

    @Override
    protected void translate(View view, float offset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.setTranslationY(offset);
        } else {
            translatePreICS(view, offset);
        }
    }

    protected void translatePreICS(View view, float offset) {
        // Custom implementation for pre-Honeycomb devices
    }
}

class AndroidAlphaManipulator extends AlphaManipulator {
    public AndroidAlphaManipulator() {
        super();
    }

    @Override
    protected void animate(View view, float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.setAlpha(alpha);
        } else {
            alphaPreICS(view, alpha);
        }
    }
}
```