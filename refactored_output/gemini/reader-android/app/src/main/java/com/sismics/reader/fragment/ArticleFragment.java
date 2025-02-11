                float startAspectRatio = (float) startBounds.width() / startBounds.height();
                float finalAspectRatio = (float) finalBounds.width() / finalBounds.height();
                if (startAspectRatio != finalAspectRatio) {
                    if (startAspectRatio < finalAspectRatio) {
                        // Image is wider than the final bounds, reduce height to match final height
                        int newHeight = Math.round(startBounds.height() / (startAspectRatio / finalAspectRatio));
                        startBounds.inset(0, (startBounds.height() - newHeight) / 2);
                    } else {
                        // Image is taller, reduce width to match final width
                        int newWidth = Math.round(startBounds.width() * (finalAspectRatio / startAspectRatio));
                        startBounds.inset((startBounds.width() - newWidth) / 2, 0);
                    }
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofInt(expandedImageView, "left", startBounds.left, finalBounds.left))
                        .with(ObjectAnimator.ofInt(expandedImageView, "top", startBounds.top, finalBounds.top))
                        .with(ObjectAnimator.ofInt(expandedImageView, "width", startBounds.width(), finalBounds.width()))
                        .with(ObjectAnimator.ofInt(expandedImageView, "height", startBounds.height(), finalBounds.height()));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        currentAnimator = null;
                        expandedImageView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        currentAnimator = null;
                        expandedImageView.setVisibility(View.GONE);
                    }
                });
                set.start();
                currentAnimator = set;

                // Show the expanded image.
                expandedImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishEnclosureZoom(View thumbView) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Hide the expanded image
        getView().findViewById(R.id.expanded_image).setVisibility(View.GONE);

        // Show the thumb image
        thumbView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEnclosureClick(JSONObject enclosure) {
        String url = enclosure.optString("url");
        if (!url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }
}