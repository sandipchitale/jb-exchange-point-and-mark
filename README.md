# Exchange Selection Start and End plugin for JetBrains IDEs

- Emacs has the exchange-point-and-mark function that lets you flips the start of selection (Emacs calls it `mark`) with end of selection (Emacs calls it point). This *Exchange Selection Start and End* plugin implements the equivalent functionality. Please note that the start of the selection may be after the end of the selection. The functionality also works when you have multiple cursors with associated, potentially different length selections.
- Support Surround selection with \[prefix\]|\[suffix\]. Must specify at least one.
  - While using surround with \[prefix\]|\[suffix\], you can use the step specification like 'Part(100, 10)' in the prefix and suffix strings
    to create string like Part100, at first cursor. Part110 at second cursor etc.

# Why

Sometimes when you have a selection with the caret at one end and you realize that you need to extend or shrink the selection at the other end. This plugin enables exactly that.

[Video of how it works](https://www.youtube.com/watch?v=kGnYTplhZXg)