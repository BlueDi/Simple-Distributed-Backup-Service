/**
 * The peers provide interfaces for:
 * - Backing up a file.
 * 		The client shall specify the file pathname and the desired replication degree.
 * - Restore a file.
 * 		The client shall specify file to restore is specified by the its pathname.
 * - Delete a file.
 * 		The client shall specify file to delete by its pathname.
 * - Manage local service storage
 * 		The client shall specify the maximum disk space in KBytes (1KByte = 1000 bytes) that can be used for storing chunks.
 * - Retrieve local service state information
 * 		This operation allows to observe the service state.
 */
package interfaces;
