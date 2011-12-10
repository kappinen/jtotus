#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <string.h>

// #include <rpcsvc/rex.h>

#include <sys/types.h>
#include <sys/ioctl.h>
#include <termios.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <stdarg.h>


#ifndef FALSE
#define FALSE 0
#endif

#ifndef TRUE
#define TRUE 1
#endif

extern int errno;
static int os_debug = 0;
static int window = 10;


static struct termios org_opts;
  int getch(void) {
  int c=0;
  struct termios new_opts;
  int res=0;

  //---- set new terminal parms --------
  memcpy(&new_opts, &org_opts, sizeof(new_opts));
  new_opts.c_lflag &= ~(ICANON | ECHO | ECHOE | ECHOK | ECHONL | ECHOPRT | ECHOKE | ICRNL);
  tcsetattr(STDIN_FILENO, TCSANOW, &new_opts);
  c=getchar();
  //------ restore old settings ---------
  res=tcsetattr(STDIN_FILENO, TCSANOW, &org_opts);
  assert(res==0);

  return(c);
}

int get_window_size()
{
  struct winsize ts;
  ioctl(0, TIOCGWINSZ, &ts);

  return ts.ws_row;
}

int os_remove_endofline(char **buff, int len)
{
  int i, removed = 0;
  char *line = *buff;
  
  for (i=0;i<len;i++)
  {
    if(line[i] == '\n' || line[i] == '\r')
      line[i] = '\0', removed++;
  }
  
  return removed;
}

int os_command(char *fmt, ...)
{
  int ret = -1;
  va_list args;
  char cmd_buff[1024*3];

  memset(&cmd_buff[0], '\0', 1024*3);
  
  va_start(args, fmt);

  vsnprintf(&cmd_buff[0], 1024*3, fmt, args);
  if (os_debug > 3)
    fprintf(stderr, "running:%s\n", cmd_buff);
  ret = system(cmd_buff);
  
  va_end(args);
  
  return ret;
}



int to_lower(int c)
{
   char letter = c;

   if(letter >= 'A')
     if (letter <= 'Z') 
     {
       letter = letter - 'A'+ 'a';
     }
//   printf("%c to -> %c\n", c , letter);
  return letter;
}

int fix_line(char *str, int size)
{
  int i = 0;
  int blank = 0;
  int count = 0;
  int next = 0;
  char line_str[10];

  memset(line_str, '\0', 10);

  for(i = 0; i< size;i++)
  {
    if (str[i] == ':')
      blank = 1, count++;

    if (blank == 1){
      if(count >= 1 && count < 2 && str[i] !=':')
        line_str[next++] = str[i];
      str[i] = '\0';
    } 
  }

return atoi(line_str);
}

int create_grep_list(int argc, char **argv)
{
  pid_t pid = 0;
  char csp_command[1024];
  int i=0, status = 0;


  os_command("rm -rf ~/.temp_search");
  os_command("clear");
  pid = fork();
  if (pid <0)
    {
      printf("[%s:%d] fork failed.\n",
                    __FUNCTION__, __LINE__);
      return 0;
    }
  else if (pid == 0)
    {/*Child*/
      int ret = 0;
      memset(csp_command, '\0', 1024);

      if (os_command("git status 2>/dev/null 1>/dev/null") == 0)
        snprintf(csp_command, 1024, "git grep -n \"");
      else
        snprintf(csp_command, 1024, "grep -iIr \"");

      for (i=1;i<argc; i++)
        strcat(csp_command, argv[i]);

      strcat(csp_command, "\" > ~/.temp_search");

      printf("Searching: %s  ...\n", csp_command);
      ret = system(csp_command);
      fprintf(stderr, "command return: %d  ...\n", ret);
      fflush(stderr);
      exit(0);
    }

  waitpid(-1, &status, 0);

  if (status != 0)
    {
      printf("Child failed:%d\n", status);
      exit(-1);
    }

  return 1;
}

int create_find_list(int argc, char **argv)
{
  pid_t pid = 0;
  char csp_command[1024];
  int i=0, status = 0;


  system("rm -rf ~/.temp_search");
  system("clear");
  pid = fork();
  if (pid <0)
    {
      printf("[%s:%d] fork failed.\n",
                    __FUNCTION__, __LINE__);
      return 0;
    }
  else if (pid == 0)
    {/*Child*/
      memset(csp_command, '\0', 1024);
      if (snprintf(csp_command,
                  1024,
                  "find . -name  '*") < 0)
        {
        return -3;
        }

      for (i=2;i<argc; i++)
        strcat(csp_command, argv[i]);

      strcat(csp_command, "*'|grep -v \"~\" |grep -v \"\\.o\" > ~/.temp_search");

      printf("Searching: %s  ...\n", csp_command);
      os_command(csp_command);
      exit(0);
    }

  waitpid(-1, &status, 0);

  if (status != 0)
    {
      printf("Child failed:%d\n", status);
      exit(-1);
    }
  return 1;
}

int create_pipe_list(int argc, char **argv)
{
  FILE *fd;
  char c;
  char file_name[1024];
  char *state_str = getenv("HOME");

  snprintf(file_name, 1024, "%s/.temp_search", state_str);

  fd = fopen(file_name, "w+");
  if(fd <0)
    return FALSE;

  while(read(0, &c, 1) > 0){
    printf("Got:%c\n", c);
      fwrite(&c, 1,1,fd);
  }

  fclose(fd);
  return TRUE;
}


int os_main_loop(int argc,
                  char **argv,
                  char *list_file)
{
  int i = 0;
  char file_name[1024];
  FILE *stm = NULL;
  char *line = NULL;
  int line_cnt = 0;
  size_t len = 0;
  int generate_list = 0;
  ssize_t read = 0;
  char *state_str = NULL;
  int num_line = 0;
  int open_file = -1;
  char line_str [100];
  int zoom = 0;
  int max_lines = 1000;
  char **window_cache = NULL;
  int delete_line = -1;
  
  if (argc <= 1)
  {
    printf("%s string-to-search\n", argv[0]);
    exit(-1);
  }
  
  while ((i = getopt(argc, argv, "fcplt:d:")) != -1) {
    switch (i) {
      case 'f': /*Find*/
        //           flags = 1;
        generate_list = 1;
        break;
      case 'c': /*Cache .temp_search*/
        generate_list = -1;
        break;
      case 'p': /*from pipe/stdion*/
        generate_list = 2;
        break;
      case 'l': /*Loop*/
        generate_list = 2;
        break;
      case 't':
        //           nsecs = atoi(optarg);
        break;
      case 'd':
        os_debug = atoi(optarg);
        break;
      default: /* ? */
        fprintf(stderr, "Usage: %string-to-search\n", argv[0]);
        exit(EXIT_FAILURE);
    }
  }

  state_str = getenv("HOME");
  
  if (state_str == NULL)
  {
    printf("HOME is not defined.\n");
    exit(-1);
  }

  //----- store old settings -----------
  i=tcgetattr(STDIN_FILENO, &org_opts);
  if(i != 0)
    printf("Error:%s\n",strerror(errno));
  // assert(i==0);
  
  refresh:
  if (generate_list == 0)
  {
    if (create_grep_list(argc, argv) != 1)
    {
      printf("Failed to create grep list\n");
      return -1;
    }
  }
  else if(generate_list == 1)
  {
    if (create_find_list(argc, argv) != 1)
    {
      printf("Failed to create find list\n");
      return -1;
    }
  }
  else if(generate_list == 2)
  {
    if (create_pipe_list(argc, argv) != 1)
    {
      printf("Failed to create find list\n");
      return -1;
    }
    /*Use cache after pipe is done.*/
    generate_list = -1;
  }
  
  memset(file_name, '\0', 1024);
  snprintf(file_name, 1024, "%s/%s", state_str, list_file);
  
  
  int c = 0;
  int start_print = 0;
  
  
  
  /* set flag |= FLAGII,
  if on & SG_TRD_COMPAT_FLAGS_STERRA*/
  do{
    c = to_lower(c);
    if (os_debug>3)
    {
      printf("Got:%d\n", c);
    }
    
    //       window = get_window_size() - 2;
    if (window != get_window_size() - 2)
    {
      window = get_window_size() - 2;
      //          if(window_cache != NULL)
      //            free(window_cache);
      //          else
      //            window_cache = malloc(window);
    }
    else
      window = get_window_size() - 2;
    
    if (c == 97) /*Up*/
    { line_cnt--; if (line_cnt<0) line_cnt = 0;}
    else if (c == 98) /*Down*/
    { line_cnt++; if(line_cnt >= max_lines) line_cnt = max_lines - 1;}
    else if (c == 10) /*Enter*/
    { open_file = line_cnt; }
    else if(c ==72) /*Home*/
    { line_cnt = 0; start_print = 0;}
    else if(c == 99) /*Start pico in file*/
    { zoom = 1;}
    else if(c == 114) /*Refresh list*/
    { goto refresh;}
    else if(c == 53) /*27,91,53,126 -> page up*/
    {
      line_cnt-=get_window_size()+1;
      start_print-=get_window_size()+1;
      if (line_cnt<0) line_cnt = 0;
      if (start_print<0) start_print = 0;
    }
    else if(c == 54) /*27,91,54,126 -> page down*/
    {
      line_cnt+=get_window_size()-1;
      start_print+=get_window_size()-1;
      if (line_cnt >= max_lines)
        line_cnt = max_lines - 1;
      if (start_print >= max_lines)
        start_print = max_lines - get_window_size();
    }
    else if(c == 'g') /* grep */
    {
      printf("grep:");
      getline(&line, &len, stdin);
      os_remove_endofline(&line, len);
      
      if (strlen(line) != 0)
      {
        if (os_debug>3)
          printf("command:%s:len2 %u\n",line, (unsigned int)len);
        
        os_command("grep \"%s\" ~/%s > ~/.temp_search2", line, list_file);
        os_command("mv ~/.temp_search2 ~/%s", list_file);
      }
    }
    else if(c == 'v' || c == 'V') /* grep -v */
    {
      printf("grep -v:");
      getline(&line, &len, stdin);
      
      os_remove_endofline(&line, len);
      
      if (strlen(line) != 0)
      {
        if (os_debug > 3)
          printf("command:%s:len2 %u\n",line,(unsigned int) len);
        
        os_command("grep -v \"%s\" ~/%s > ~/.temp_search2", line, list_file);
        os_command("mv ~/.temp_search2 ~/%s", list_file);
      }
    }
    else if (c == 'd')
    {
      delete_line = line_cnt;
    }
    
    
    
    if (os_debug > 4)
    {
      fprintf(stderr, "line_cnt:%d start_print:%d window:%d\n", line_cnt, start_print, window);
      fflush(stderr);
    }
    
    
    
    if(line_cnt > (start_print + window))
      start_print++;
    else if(line_cnt < start_print)
      start_print--;
    
    system("clear");
    stm = fopen(file_name, "r");
    if (stm == NULL)
    {
      printf("%s\n", file_name);
      return -1;
    }
    
    int tmp = 0;
    while ((read = getline(&line, &len, stm)) != -1)
    {
      if (tmp == delete_line && tmp >= 0)
      {
        os_remove_endofline(&line, len);
        os_command("grep -v \"%s\" ~/%s > ~/.temp_search2", line, list_file);
        os_command("mv ~/.temp_search2 ~/%s", list_file);
        delete_line = -1;
        /*FIXME:bug!*/
        continue;
      }
      
      if((tmp >= start_print) &&
        (tmp <= (start_print + window)))
      {
        fprintf(stdout, "%s", line_cnt == tmp ? "\033[22;31m-> " : " ");
        fprintf(stdout, "%s", line);
        fprintf(stdout, "%s", line_cnt == tmp ? "\033[0m" : "");
      }
      
      if (tmp == open_file)
      {
        memset(line_str, '\0', 100);
        num_line = fix_line(line, len);
        
        os_command("kate -u -l %d %s", num_line, line);
        open_file = -1;
        //               goto final;
      }
      else if (zoom && tmp == line_cnt)
      {
        num_line = fix_line(line, len);
        os_command("pico +%d %s", num_line, line);
        zoom = 0;
        //             goto refresh;
      }
      
      tmp++;
    }
    max_lines = tmp;
    /*File or data is not Not found*/
    if(tmp == 0 )
      goto final;
    
    fflush(stdout);
    fclose(stm);


    
    c=getch();
  }while (c != 'q' && c != 'Q');

final:
  fprintf(stdout, "Done.\n");
  
return 1;
}





int main(int argc, char **argv)
{
 

  os_main_loop(argc, argv, ".temp_search");

  return TRUE;
}


