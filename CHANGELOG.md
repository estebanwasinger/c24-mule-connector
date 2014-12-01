C24 Mule Connector Release Notes
================================

## v1.2.0

### Supported C24-iO versions

* C24 4.6.x

### Supported Mule Runtime Versions

* 3.4.2+

### New Features and Functionality

The Transform Advanced operation has changed to take a list of Objects instead of a list of Strings. All existing usages will likely need to be changed in order to use the new version. Please see the user guide for details on how to use the new version of the operation.

### Closed Issues in this release

None

### Known Issues in this release

None


## v1.1.1

### Supported C24-iO versions

* C24 4.6.x

### Supported Mule Runtime Versions

* 3.4.2+

### New Features and Functionality

None

### Closed Issues in this release

Explicitly close payload InputStream and Readers once parsing has completed.

### Known Issues in this release

None




## v1.1.0


### Supported C24-iO versions

* C24 4.6.x

### Supported Mule Runtime Versions

* 3.4.2+

### New Features and Functionality

* New parse, validate, transform, transformAdvanced and marshal operations
* Existing one-shot transform operation renamed to convert

### Closed Issues in this release

None

### Known Issues in this release

None



## v1.0.1

### Supported C24-iO versions

* C24 4.6.x

### Supported Mule Runtime Versions

* 3.4.2+


### New Features and Functionality

* Added support for Mule Runtime 3.4.2+

### Closed Issues in this release

Nonde

### Known Issues in this release

None



## v1.0.0


### Supported C24-iO versions

* C24 4.6.x

### Supported Mule Runtime Versions

* 3.5.0+


### New Features and Functionality

* Initial version supporting C24 iO transforms (and implicit parsing and generation).

### Closed Issues in this release

N/A

### Known Issues in this release

None
