# Image Processing and Computer Vision

## Corrupted Image Correction

Given a corrupted image the program should measure and correct it as follows:

- measured noise using variance, fix using a median filter
- measured blur using a soble kernel, fix using unsharp filter
- measured color collapsing using min and max intensities, fix using contrast stretching

<table>
  <tr>
    <td style="text-align: center;">
      <img src="./docs/imgs/8.jpg" alt="Image 1"/>
      <br>
      <em>Corrupted</em>
    </td>
    <td style="text-align: center;">
      <img src="./docs/imgs/1.jpg" alt="Image 2"/>
      <br>
      <em>Fixed</em>
    </td>
  </tr>
</table>



## Stereo Vision and Disparity

- Disparity estimation using SSD

![Image](./docs/imgs/stereo_vision.jpg)

## Image Processing

- Color correction
- Blending images
- Fitting an image inside a frame in another image
- Fitting an image inside a frame with a different prespective
- Applying different filters

![Image](./docs/imgs/correction.png)
![Image](./docs/imgs/fitting.png)
![Image](./docs/imgs/perspective.png)
