# Resource manager plugin for Play Framework 2
By generating a sha1 checksum of a file and storing it in a directory structure like `res/5564/ac5e/5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg` we have a unique identifier for that specific file.

> it's been proven that filesystems such as ext4 and ntfs don't like it when you save to many files in the same directory, but by splitting the hash into three levels, we can easily
> navigate the heirarchy even if there are thousands of files.


- Source code: [https://github.com/digiPlant/play-res][source]
- CI: [http://travis-ci.org/#!/digiPlant/play-res][ci]

play-res is an Open Source project under the Apache License v2.

[source]: https://github.com/digiPlant/play-res
[ci]: http://travis-ci.org/#!/digiPlant/play-res
