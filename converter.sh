for ((a=8421; a <= 8433; a++))
do
    convert -filter Cubic -resize 720 "sq_phone_calib/IMG_$a.JPG" "sq_shrunk_images/$a.jpg"
done

