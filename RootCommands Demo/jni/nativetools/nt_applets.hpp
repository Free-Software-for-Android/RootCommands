// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#if !defined(NATIVETOOLS_APPLETS_HPP)
	#define  NATIVETOOLS_APPLETS_HPP

	#define APPLET(x) int x(int argc, char** argv, char** env)
	
	typedef struct {
		const char* keyword;
		int (*fn)(int argc, char** argv, char** env);
	} APPLET_DEF;

	#define APPLETS_COUNT (int)(sizeof(applets)/sizeof(APPLET_DEF))

	// ********************************
	// C++ Applets are registered here:
	// ********************************
	APPLET(nt_df);
	APPLET(nt_du);
	APPLET(nt_file_exists);
	APPLET(nt_get_owner);
	APPLET(nt_list_links);
	APPLET(nt_mount_loop);
	APPLET(nt_mount_read_write);
	APPLET(nt_read_file);
	APPLET(nt_recursive_chown);
	APPLET(nt_recursive_cp);
	APPLET(nt_recursive_crawl);
	APPLET(nt_recursive_remove);

	// ********************************
	// C Applets are registered here:
	// ********************************
	#ifdef __cplusplus
	extern "C" {
	#endif

	#ifdef __cplusplus
	}
	#endif

	APPLET_DEF applets[] = {
		{"df", &nt_df},
		{"du", &nt_du},
		{"fe", &nt_file_exists},
		{"go", &nt_get_owner},
		{"ll", &nt_list_links},
		/*
		{"ml", &nt_mount_loop},
		{"mr", &nt_mount_read_write},
		{"mw", &nt_mount_read_write},
		*/
		{"rf", &nt_read_file},
		{"co", &nt_recursive_chown},
		{"cp", &nt_recursive_cp},
		{"cr", &nt_recursive_crawl},
		{"rm", &nt_recursive_remove}
	};
#endif /* NATIVETOOLS_APPLETS_HPP */
